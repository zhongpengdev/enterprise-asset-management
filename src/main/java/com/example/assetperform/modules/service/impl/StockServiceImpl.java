package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.common.security.SecurityUtils;
import com.example.assetperform.common.utils.CodeGenerator;
import com.example.assetperform.modules.domain.dto.StockInDTO;
import com.example.assetperform.modules.domain.dto.StockQueryDTO;
import com.example.assetperform.modules.domain.dto.StockRecordQueryDTO;
import com.example.assetperform.modules.domain.entity.ItemEntity;
import com.example.assetperform.modules.domain.entity.StockEntity;
import com.example.assetperform.modules.domain.entity.StockRecordEntity;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.entity.WarehouseEntity;
import com.example.assetperform.modules.domain.vo.StockRecordVO;
import com.example.assetperform.modules.domain.vo.StockVO;
import com.example.assetperform.modules.mapper.StockMapper;
import com.example.assetperform.modules.mapper.StockRecordMapper;
import com.example.assetperform.modules.service.ItemService;
import com.example.assetperform.modules.service.StockService;
import com.example.assetperform.modules.service.UserService;
import com.example.assetperform.modules.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, StockEntity> implements StockService {

    private final StockRecordMapper stockRecordMapper;
    private final WarehouseService warehouseService;
    private final ItemService itemService;
    private final UserService userService;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(StockInDTO dto) {
        // 1. 核验目标仓库是否存在且有效
        WarehouseEntity warehouse = warehouseService.getById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("目标仓库不存在！");
        }
        if (warehouse.getStatus() != null && warehouse.getStatus() == 1) {
            throw new BusinessException("该仓库已被停用，禁止办理物资入库！");
        }

        // 2. 核验目标物资档案是否存在且有效
        ItemEntity item = itemService.getById(dto.getItemId());
        if (item == null) {
            throw new BusinessException("目标物资档案不存在！");
        }
        if (item.getStatus() != null && item.getStatus() == 1) {
            throw new BusinessException("该物资已被停用，禁止办理入库！");
        }

        // 3. 查询或初始化库存记录
        StockEntity stock = this.lambdaQuery()
                .eq(StockEntity::getWarehouseId, dto.getWarehouseId())
                .eq(StockEntity::getItemId, dto.getItemId())
                .one();

        int beforeStock;
        int afterStock;

        if (stock == null) {
            beforeStock = 0;
            afterStock = dto.getNum();

            stock = new StockEntity();
            stock.setWarehouseId(dto.getWarehouseId());
            stock.setItemId(dto.getItemId());
            stock.setActualStock(afterStock);
            stock.setLockedStock(0);
            this.save(stock);
        } else {
            beforeStock = stock.getActualStock() != null ? stock.getActualStock() : 0;
            afterStock = beforeStock + dto.getNum();

            stock.setActualStock(afterStock);
            this.updateById(stock);
        }

        // 4. 生成库存变动流水记录（LS开头唯一单号）
        String recordNo = codeGenerator.generateCode("LS");
        Long currentUserId = SecurityUtils.getUserId();

        StockRecordEntity record = new StockRecordEntity();
        record.setRecordNo(recordNo);
        record.setWarehouseId(dto.getWarehouseId());
        record.setItemId(dto.getItemId());
        record.setType(1); // 1-采购入库
        record.setChangeNum(dto.getNum());
        record.setBeforeStock(beforeStock);
        record.setAfterStock(afterStock);
        record.setOperatorId(currentUserId != null ? currentUserId : (warehouse.getManagerId() != null ? warehouse.getManagerId() : 1L));
        record.setBizNo(dto.getBizNo());
        record.setRemark(dto.getRemark());
        record.setCreateTime(LocalDateTime.now());

        stockRecordMapper.insert(record);
    }

    @Override
    public Page<StockVO> listPage(StockQueryDTO query) {
        // 如果不需要预警筛选，走直接物理分页
        if (!Boolean.TRUE.equals(query.getOnlyWarning())) {
            Page<StockEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
            Page<StockEntity> stockPage = this.lambdaQuery()
                    .eq(query.getWarehouseId() != null, StockEntity::getWarehouseId, query.getWarehouseId())
                    .eq(query.getItemId() != null, StockEntity::getItemId, query.getItemId())
                    .orderByDesc(StockEntity::getUpdateTime)
                    .page(page);

            List<StockEntity> records = stockPage.getRecords();
            if (CollectionUtils.isEmpty(records)) {
                return new Page<>(query.getPageNum(), query.getPageSize(), 0);
            }

            List<StockVO> voList = assembleStockVOList(records);
            Page<StockVO> voPage = new Page<>(stockPage.getCurrent(), stockPage.getSize(), stockPage.getTotal());
            voPage.setRecords(voList);
            return voPage;
        }

        // 若需要仅看安全预警（availableStock <= safetyStock），全量查出后内存过滤再物理切片
        List<StockEntity> allStocks = this.lambdaQuery()
                .eq(query.getWarehouseId() != null, StockEntity::getWarehouseId, query.getWarehouseId())
                .eq(query.getItemId() != null, StockEntity::getItemId, query.getItemId())
                .orderByDesc(StockEntity::getUpdateTime)
                .list();

        if (CollectionUtils.isEmpty(allStocks)) {
            return new Page<>(query.getPageNum(), query.getPageSize(), 0);
        }

        List<StockVO> allVOs = assembleStockVOList(allStocks);
        List<StockVO> warningVOs = allVOs.stream()
                .filter(vo -> Boolean.TRUE.equals(vo.getIsWarning()))
                .collect(Collectors.toList());

        int total = warningVOs.size();
        int fromIndex = (int) Math.min((query.getPageNum() - 1) * query.getPageSize(), total);
        int toIndex = (int) Math.min(fromIndex + query.getPageSize(), total);
        List<StockVO> pagedList = warningVOs.subList(fromIndex, toIndex);

        Page<StockVO> voPage = new Page<>(query.getPageNum(), query.getPageSize(), total);
        voPage.setRecords(pagedList);
        return voPage;
    }

    @Override
    public Page<StockRecordVO> listRecords(StockRecordQueryDTO query) {
        Page<StockRecordEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getWarehouseId() != null, StockRecordEntity::getWarehouseId, query.getWarehouseId())
                .eq(query.getItemId() != null, StockRecordEntity::getItemId, query.getItemId())
                .eq(query.getType() != null, StockRecordEntity::getType, query.getType())
                .eq(StringUtils.hasText(query.getRecordNo()), StockRecordEntity::getRecordNo, query.getRecordNo())
                .eq(StringUtils.hasText(query.getBizNo()), StockRecordEntity::getBizNo, query.getBizNo())
                .orderByDesc(StockRecordEntity::getCreateTime);

        Page<StockRecordEntity> recordPage = stockRecordMapper.selectPage(page, wrapper);
        List<StockRecordEntity> records = recordPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new Page<>(query.getPageNum(), query.getPageSize(), 0);
        }

        // 批量提取关联 ID
        Set<Long> warehouseIds = records.stream().map(StockRecordEntity::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> itemIds = records.stream().map(StockRecordEntity::getItemId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> operatorIds = records.stream().map(StockRecordEntity::getOperatorId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 批量查询映射
        Map<Long, String> warehouseMap = CollectionUtils.isEmpty(warehouseIds) ? Collections.emptyMap() :
                warehouseService.listByIds(warehouseIds).stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        Map<Long, ItemEntity> itemMap = CollectionUtils.isEmpty(itemIds) ? Collections.emptyMap() :
                itemService.listByIds(itemIds).stream().collect(Collectors.toMap(ItemEntity::getId, it -> it));

        Map<Long, String> userMap = CollectionUtils.isEmpty(operatorIds) ? Collections.emptyMap() :
                userService.listByIds(operatorIds).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));

        // 组装 VO
        List<StockRecordVO> voList = records.stream().map(r -> {
            StockRecordVO vo = new StockRecordVO();
            vo.setId(r.getId());
            vo.setRecordNo(r.getRecordNo());
            vo.setWarehouseId(r.getWarehouseId());
            vo.setWarehouseName(warehouseMap.get(r.getWarehouseId()));
            vo.setItemId(r.getItemId());

            ItemEntity it = itemMap.get(r.getItemId());
            if (it != null) {
                vo.setItemName(it.getName());
                vo.setItemCode(it.getCode());
            }

            vo.setType(r.getType());
            vo.setTypeName(resolveTypeName(r.getType()));
            vo.setChangeNum(r.getChangeNum());
            vo.setBeforeStock(r.getBeforeStock());
            vo.setAfterStock(r.getAfterStock());
            vo.setOperatorId(r.getOperatorId());
            vo.setOperatorName(userMap.get(r.getOperatorId()));
            vo.setBizNo(r.getBizNo());
            vo.setRemark(r.getRemark());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        Page<StockRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 批量关联物资与仓库，组装 StockVO
     */
    private List<StockVO> assembleStockVOList(List<StockEntity> stocks) {
        Set<Long> warehouseIds = stocks.stream().map(StockEntity::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> itemIds = stocks.stream().map(StockEntity::getItemId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> warehouseMap = CollectionUtils.isEmpty(warehouseIds) ? Collections.emptyMap() :
                warehouseService.listByIds(warehouseIds).stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        Map<Long, ItemEntity> itemMap = CollectionUtils.isEmpty(itemIds) ? Collections.emptyMap() :
                itemService.listByIds(itemIds).stream().collect(Collectors.toMap(ItemEntity::getId, it -> it));

        return stocks.stream().map(s -> {
            StockVO vo = new StockVO();
            vo.setId(s.getId());
            vo.setWarehouseId(s.getWarehouseId());
            vo.setWarehouseName(warehouseMap.get(s.getWarehouseId()));
            vo.setItemId(s.getItemId());

            ItemEntity item = itemMap.get(s.getItemId());
            if (item != null) {
                vo.setItemName(item.getName());
                vo.setItemCode(item.getCode());
                vo.setModel(item.getModel());
                vo.setUnit(item.getUnit());
                vo.setSafetyStock(item.getSafetyStock());
            }

            int actual = s.getActualStock() != null ? s.getActualStock() : 0;
            int locked = s.getLockedStock() != null ? s.getLockedStock() : 0;
            int available = actual - locked;

            vo.setActualStock(actual);
            vo.setLockedStock(locked);
            vo.setAvailableStock(available);

            int safety = (item != null && item.getSafetyStock() != null) ? item.getSafetyStock() : 0;
            vo.setIsWarning(available <= safety);

            return vo;
        }).collect(Collectors.toList());
    }

    private String resolveTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "采购入库";
            case 2 -> "申领出库";
            case 3 -> "退库入库";
            case 4 -> "盘点调整";
            case 5 -> "报废出库";
            default -> "其他变动";
        };
    }
}
