package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.common.utils.CodeGenerator;
import com.example.assetperform.modules.domain.dto.DeviceQueryDTO;
import com.example.assetperform.modules.domain.dto.DeviceRegisterDTO;
import com.example.assetperform.modules.domain.entity.DeptEntity;
import com.example.assetperform.modules.domain.entity.DeviceEntity;
import com.example.assetperform.modules.domain.entity.ItemEntity;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.vo.DeviceVO;
import com.example.assetperform.modules.mapper.DeviceMapper;
import com.example.assetperform.modules.service.DeptService;
import com.example.assetperform.modules.service.DeviceService;
import com.example.assetperform.modules.service.ItemService;
import com.example.assetperform.modules.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DeviceEntity> implements DeviceService {

    private final ItemService itemService;
    private final UserService userService;
    private final DeptService deptService;
    private final CodeGenerator codeGenerator;

    @Override
    public void registerDevice(DeviceRegisterDTO dto) {
        // 1. 核验关联物资档案是否存在
        ItemEntity item = itemService.getById(dto.getItemId());
        if (item == null) {
            throw new BusinessException("所选物资档案不存在！");
        }

        // 2. 【核心业务规则】必须是固定资产（type=2）才能建立设备卡片
        if (item.getType() != 2) {
            throw new BusinessException("该物资属于消耗品，无法建立独立固定资产设备卡片！");
        }

        // 3. 构建实体并自动生成 ZC 开头的资产编号
        DeviceEntity deviceEntity = new DeviceEntity();
        BeanUtils.copyProperties(dto, deviceEntity);

        String assetCode = codeGenerator.generateCode("ZC");
        deviceEntity.setAssetCode(assetCode);
        deviceEntity.setStatus(0); // 初始状态：闲置在库

        // 4. 保存入库
        this.save(deviceEntity);
    }

    @Override
    public Page<DeviceVO> listPage(DeviceQueryDTO query) {
        // 1. 物理分页查询设备表
        Page<DeviceEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        Page<DeviceEntity> devicePage = this.lambdaQuery()
                .eq(StringUtils.hasText(query.getAssetCode()), DeviceEntity::getAssetCode, query.getAssetCode())
                .eq(StringUtils.hasText(query.getSerialNo()), DeviceEntity::getSerialNo, query.getSerialNo())
                .eq(query.getItemId() != null, DeviceEntity::getItemId, query.getItemId())
                .eq(query.getUserId() != null, DeviceEntity::getUserId, query.getUserId())
                .eq(query.getDeptId() != null, DeviceEntity::getDeptId, query.getDeptId())
                .eq(query.getStatus() != null, DeviceEntity::getStatus, query.getStatus())
                .orderByDesc(DeviceEntity::getCreateTime)
                .page(page);

        List<DeviceEntity> records = devicePage.getRecords();
        if (records.isEmpty()) {
            Page<DeviceVO> emptyPage = new Page<>(devicePage.getCurrent(), devicePage.getSize(), devicePage.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        // 2. 批量聚合关联数据（物资信息、员工姓名、部门名称）
        Set<Long> itemIds = records.stream().map(DeviceEntity::getItemId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(DeviceEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> deptIds = records.stream().map(DeviceEntity::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, ItemEntity> itemMap = itemIds.isEmpty() ? Collections.emptyMap() :
                itemService.listByIds(itemIds).stream().collect(Collectors.toMap(ItemEntity::getId, item -> item));

        Map<Long, String> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userService.listByIds(userIds).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));

        Map<Long, String> deptMap = deptIds.isEmpty() ? Collections.emptyMap() :
                deptService.listByIds(deptIds).stream().collect(Collectors.toMap(DeptEntity::getId, DeptEntity::getName));

        // 3. 内存极速组装完整卡片 VO
        List<DeviceVO> voList = records.stream().map(device -> {
            DeviceVO vo = new DeviceVO();
            BeanUtils.copyProperties(device, vo);

            // 填充物资规格与品牌
            ItemEntity item = itemMap.get(device.getItemId());
            if (item != null) {
                vo.setItemName(item.getName());
                vo.setBrand(item.getBrand());
                vo.setModel(item.getModel());
            }

            // 填充当前使用人与部门
            vo.setUserName(userMap.getOrDefault(device.getUserId(), "暂无 (闲置)"));
            vo.setDeptName(deptMap.getOrDefault(device.getDeptId(), "暂无 (在库)"));

            return vo;
        }).toList();

        // 4. 封装返回
        Page<DeviceVO> resultPage = new Page<>(devicePage.getCurrent(), devicePage.getSize(), devicePage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
}