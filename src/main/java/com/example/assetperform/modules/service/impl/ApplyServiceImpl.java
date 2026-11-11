package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.modules.domain.dto.ApplyQueryDTO;
import com.example.assetperform.modules.domain.entity.*;
import com.example.assetperform.modules.domain.vo.ApplyOrderVO;
import com.example.assetperform.modules.mapper.ApplyOrderMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.security.SecurityUtils;
import com.example.assetperform.common.utils.CodeGenerator;
import com.example.assetperform.config.RabbitConfig;
import com.example.assetperform.modules.domain.dto.ApplyAuditDTO;
import com.example.assetperform.modules.domain.dto.ApplyCreateDTO;
import com.example.assetperform.modules.mapper.ApplyItemMapper;
import com.example.assetperform.modules.mapper.StockRecordMapper;
import com.example.assetperform.modules.service.ApplyService;
import com.example.assetperform.modules.service.DeptService;
import com.example.assetperform.modules.service.ItemService;
import com.example.assetperform.modules.service.StockService;
import com.example.assetperform.modules.service.UserService;
import com.example.assetperform.modules.service.WarehouseService;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor 
public class ApplyServiceImpl extends ServiceImpl<ApplyOrderMapper, ApplyOrderEntity> implements ApplyService{

    private final ApplyItemMapper applyItemMapper;
    private final StockService stockService;
    private final StockRecordMapper stockRecordMapper;
    private final UserService userService;
    private final DeptService deptService;
    private final WarehouseService warehouseService;
    private final ItemService itemService;
    private final CodeGenerator codeGenerator;

    // Redisson 依赖注入，用户申领互斥锁
    private final RedissonClient redissonClient;

    // 注入MQ
    private final RabbitTemplate rabbitTemplate;

    @Override 
    @Transactional (rollbackFor = Exception.class)
    public void createApply(ApplyCreateDTO applyCreateDTO) {
        
        // 从ThreadLocal获取用户的ID
        Long userId = SecurityUtils.getUserId();
        // 获取用户实体
        UserEntity currentUser = userService.getById(userId);

        if(currentUser == null) {
            throw new BusinessException("当前登陆用户不存在");
        }

        // 逐项检查申请商品的可用库存，并增加预占
        for(ApplyCreateDTO.ApplyItemParm itemParm : applyCreateDTO.getItems()) {

            // 细粒度key,仓库+物资维度加锁，不同的物资用户在申领的时候互不影响
            String lockKey = "lock:stock:" + itemParm.getWarehouseId() + ":" + itemParm.getItemId();
            // RLOCK
            RLock lock = redissonClient.getLock(lockKey);
            boolean isLocked = false;

            // 尝试获取锁
            try{
                // 尝试获取锁，最多3秒，10秒后释放锁
                /**
                 * lock() 拿不到会一直死等，高并发时会把 Tomcat 线程池占满挂掉；
                 * tryLock 最多等 3 秒，拿不到及时返回给用户“排队中”，体验更好。
                 *
                 * 为什么要有 10 秒自动过期？
                 * 万一执行到一半服务器断电或挂了，10 秒后 Redis 自动删锁，避免造成永久死锁。
                 *
                 * 为什么在 finally 里加 lock.isHeldByCurrentThread()？
                 * 防止自己的业务执行太慢锁已经过期、被别人抢走后，自己回头又把别人的锁给释放了（防误删）。
                 */
                isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);

                if(!isLocked) {
                    throw new BusinessException("出错了，请稍后重试");
                }

                // 临界区代码

                // 检查stock,还有多少
                StockEntity stock = stockService.lambdaQuery()
                        .eq(StockEntity::getWarehouseId, itemParm.getWarehouseId())
                        .eq(StockEntity::getItemId, itemParm.getItemId())
                        .one();

                // 算available
                int actual = (stock != null && stock.getActualStock() != null) ? stock.getActualStock() : 0;
                int locked = (stock != null && stock.getLockedStock() != null) ? stock.getLockedStock() : 0;
                int available = actual - locked;

                if(available < itemParm.getApplyNum()) {
                    throw new BusinessException("物资： [ID: ]" + itemParm.getItemId() + "] 可申请库存不足， 当前还剩" + available);
                }

                // 增加商品的预占库存
                stock.setLockedStock(stock.getLockedStock() + itemParm.getApplyNum());
                stockService.updateById(stock);
            
            }catch (InterruptedException e){
                throw new BusinessException("出错了，请稍候重试");
            }finally {
                // 如果是当前线程持有锁，则释放，防止释放了别人的
                if(isLocked && lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }

        // 保存申领主表
        ApplyOrderEntity applyOrder = new ApplyOrderEntity();
        applyOrder.setOrderNo(codeGenerator.generateCode("SL"));
        applyOrder.setApplicantId(userId);
        applyOrder.setDeptId(currentUser.getDeptId());
        applyOrder.setStatus(0); // 0-待审批
        applyOrder.setReason(applyCreateDTO.getReason());
        this.save(applyOrder);

        // 批量保存明细表
        for(ApplyCreateDTO.ApplyItemParm itemParam : applyCreateDTO.getItems()) {
            ApplyItemEntity itemEntity = new ApplyItemEntity();
            itemEntity.setApplyId(applyOrder.getId());

            itemEntity.setWarehouseId(itemParam.getWarehouseId());
            itemEntity.setItemId(itemParam.getItemId());
            itemEntity.setApplyNum(itemParam.getApplyNum());
            itemEntity.setActualNum(0);

            applyItemMapper.insert(itemEntity);
        }

        // 发消息给MQ, 自动倒计时
        rabbitTemplate.convertAndSend(RabbitConfig.APPLY_DELAY_QUEUE,
          applyOrder.getId());
    }

    // 主管审批的结果发到这里
    @Override 
    @Transactional (rollbackFor = Exception.class)
    public void auditApply(ApplyAuditDTO dto) {
        
        // 申请的id
        ApplyOrderEntity order = this.getById(dto.getApplyId());
        
        if(order == null) {
            throw new BusinessException("申领单不存在");
        }

        if(order.getStatus() != 0) {
            throw new BusinessException("只能处理 【待审批】的单子");
        }

        Long auditUserId = SecurityUtils.getUserId();
        order.setAuditUserId(auditUserId);
        order.setAuditTime(LocalDateTime.now());
        order.setAuditRemark(dto.getAuditRemark());

        if(Boolean.TRUE.equals(dto.getPass())){
            // 审核通过了 -> 更新order的状态
            order.setStatus(1);
        }else{
            // 申请驳回 释放预占用库存
            order.setStatus(2);

            // 将要申请的item拿到
            List<ApplyItemEntity> items = applyItemMapper.selectList(
                    new LambdaQueryWrapper<ApplyItemEntity>()
                            .eq(ApplyItemEntity::getApplyId, order.getId())
            );

            for (ApplyItemEntity item : items) {
                StockEntity stock = stockService.lambdaQuery()
                        .eq(StockEntity::getWarehouseId, item.getWarehouseId())
                        .eq(StockEntity::getItemId, item.getItemId())
                        .one();
                if(stock != null){
                    // 更新stock的锁定库存信息
                    int locked = stock.getLockedStock() != null ? stock.getLockedStock() : 0;
                    stock.setLockedStock(Math.max(0, locked - item.getApplyNum()));
                    stockService.updateById(stock);
                }
            }
        }
        this.updateById(order);
    }

    // 员工点击确认申领物资（出库）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverApply(Long applyId) {
        ApplyOrderEntity order = this.getById(applyId);
        if (order == null) {
            throw new BusinessException("申领单不存在！");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有 [待发放] 状态的单据才能执行实际出库发放！");
        }
        List<ApplyItemEntity> items = applyItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApplyItemEntity>()
                        .eq(ApplyItemEntity::getApplyId, order.getId())
        );

        Long operatorId = SecurityUtils.getUserId();

        for (ApplyItemEntity item : items) {
            StockEntity stock = stockService.lambdaQuery()
                    .eq(StockEntity::getWarehouseId, item.getWarehouseId())
                    .eq(StockEntity::getItemId, item.getItemId())
                    .one();
            if (stock == null) {
                throw new BusinessException("库存数据异常，未找到对应仓库物资！");
            }
            int beforeActual = stock.getActualStock() != null ? stock.getActualStock() : 0;
            int beforeLocked = stock.getLockedStock() != null ? stock.getLockedStock() : 0;
            if (beforeActual < item.getApplyNum()) {
                throw new BusinessException("物理实际库存不足，无法发放！");
            }
            // 扣减物理库存，释放预占库存
            int afterActual = beforeActual - item.getApplyNum();
            stock.setActualStock(afterActual);
            stock.setLockedStock(Math.max(0, beforeLocked - item.getApplyNum()));
            stockService.updateById(stock);
            // 更新明细的实际发放数
            item.setActualNum(item.getApplyNum());
            applyItemMapper.updateById(item);
            // 插入出库流水
            StockRecordEntity record = new StockRecordEntity();
            record.setRecordNo(codeGenerator.generateCode("LS"));
            record.setWarehouseId(item.getWarehouseId());
            record.setItemId(item.getItemId());
            record.setType(2); // 2-申领出库
            record.setChangeNum(-item.getApplyNum());
            record.setBeforeStock(beforeActual);
            record.setAfterStock(afterActual);
            record.setOperatorId(operatorId != null ? operatorId : 1L);
            record.setBizNo(order.getOrderNo());
            record.setRemark("申领单出库发放");
            record.setCreateTime(LocalDateTime.now());
            stockRecordMapper.insert(record);
        }
        // 更新单据状态为 3-已完成
        order.setStatus(3);
        this.updateById(order);
    }

    // 分页查询已经申领的内容
    @Override
    public Page<ApplyOrderVO> listPage(ApplyQueryDTO query) {
        Page<ApplyOrderEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ApplyOrderEntity> orderPage = this.lambdaQuery()
                .eq(StringUtils.hasText(query.getOrderNo()), ApplyOrderEntity::getOrderNo, query.getOrderNo())
                .eq(query.getStatus() != null, ApplyOrderEntity::getStatus, query.getStatus())
                .orderByDesc(ApplyOrderEntity::getCreateTime)
                .page(page);
        List<ApplyOrderEntity> orders = orderPage.getRecords();
        if (CollectionUtils.isEmpty(orders)) {
            return new Page<>(query.getPageNum(), query.getPageSize(), 0);
        }
        // 批量提取关联用户、部门与主单 ID，杜绝 N+1
        Set<Long> userIds = new HashSet<>();
        Set<Long> deptIds = new HashSet<>();
        List<Long> orderIds = new ArrayList<>();
        for (ApplyOrderEntity o : orders) {
            orderIds.add(o.getId());
            if (o.getApplicantId() != null) userIds.add(o.getApplicantId());
            if (o.getAuditUserId() != null) userIds.add(o.getAuditUserId());
            if (o.getDeptId() != null) deptIds.add(o.getDeptId());
        }
        Map<Long, String> userMap = CollectionUtils.isEmpty(userIds) ? Collections.emptyMap() :
                userService.listByIds(userIds).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));
        Map<Long, String> deptMap = CollectionUtils.isEmpty(deptIds) ? Collections.emptyMap() :
                deptService.listByIds(deptIds).stream().collect(Collectors.toMap(DeptEntity::getId, DeptEntity::getName));
        // 批量查询明细并按 applyId 分组
        List<ApplyItemEntity> allItems = applyItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApplyItemEntity>()
                        .in(ApplyItemEntity::getApplyId, orderIds)
        );

        Set<Long> warehouseIds = allItems.stream().map(ApplyItemEntity::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());

        Set<Long> itemIds = allItems.stream().map(ApplyItemEntity::getItemId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> warehouseMap = CollectionUtils.isEmpty(warehouseIds) ? Collections.emptyMap() :
                warehouseService.listByIds(warehouseIds).stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        Map<Long, ItemEntity> itemMap;
        itemMap = CollectionUtils.isEmpty(itemIds) ? Collections.emptyMap() :
                itemService.listByIds(itemIds).stream().collect(Collectors.toMap(ItemEntity::getId, it -> it));

        Map<Long, List<ApplyItemEntity>> itemGroup = allItems.stream().collect(Collectors.groupingBy(ApplyItemEntity::getApplyId));

        // 组装 VO
        List<ApplyOrderVO> voList = orders.stream().map(o -> {
            ApplyOrderVO vo = new ApplyOrderVO();

            BeanUtils.copyProperties(o, vo);

            vo.setApplicantName(userMap.get(o.getApplicantId()));

            vo.setAuditUserName(userMap.get(o.getAuditUserId()));

            vo.setDeptName(deptMap.get(o.getDeptId()));

            List<ApplyItemEntity> subItems = itemGroup.getOrDefault(o.getId(), Collections.emptyList());

            List<ApplyOrderVO.ApplyItemVO> subVOs = subItems.stream().map(it -> {
                ApplyOrderVO.ApplyItemVO itemVO = new ApplyOrderVO.ApplyItemVO();
                itemVO.setId(it.getId());
                itemVO.setWarehouseId(it.getWarehouseId());
                itemVO.setWarehouseName(warehouseMap.get(it.getWarehouseId()));
                itemVO.setItemId(it.getItemId());
                ItemEntity itemObj = itemMap.get(it.getItemId());
                if (itemObj != null) {
                    itemVO.setItemName(itemObj.getName());
                    itemVO.setItemCode(itemObj.getCode());
                    itemVO.setModel(itemObj.getModel());
                    itemVO.setUnit(itemObj.getUnit());
                }
                itemVO.setApplyNum(it.getApplyNum());
                itemVO.setActualNum(it.getActualNum());
                return itemVO;
            }).collect(Collectors.toList());
            vo.setItems(subVOs);
            return vo;
        }).collect(Collectors.toList());
        Page<ApplyOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}

