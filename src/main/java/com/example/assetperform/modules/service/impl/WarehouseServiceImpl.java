package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.modules.domain.dto.WarehouseCreateDTO;
import com.example.assetperform.modules.domain.dto.WarehouseQueryDTO;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.entity.WarehouseEntity;
import com.example.assetperform.modules.domain.vo.WarehouseVO;
import com.example.assetperform.modules.mapper.WarehouseMapper;
import com.example.assetperform.modules.service.UserService;
import com.example.assetperform.modules.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, WarehouseEntity> implements WarehouseService {

    private final UserService userService;

    @Override
    public void createWarehouse(WarehouseCreateDTO dto) {
        // 1. 唯一性校验：仓库编码不可重复
        Long count = this.lambdaQuery()
                .eq(WarehouseEntity::getCode, dto.getCode())
                .count();
        if (count > 0) {
            throw new BusinessException("仓库编码已存在，请勿重复创建！");
        }

        // 2. 校验负责人是否存在（若指定）
        if (dto.getManagerId() != null) {
            UserEntity user = userService.getById(dto.getManagerId());
            if (user == null) {
                throw new BusinessException("指定的仓库负责人不存在！");
            }
        }

        // 3. 构造实体并持久化
        WarehouseEntity warehouseEntity = new WarehouseEntity();
        BeanUtils.copyProperties(dto, warehouseEntity);
        this.save(warehouseEntity);
    }

    @Override
    public Page<WarehouseVO> listPage(WarehouseQueryDTO query) {
        // 1. 构建分页与查询条件
        Page<WarehouseEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<WarehouseEntity> warehousePage = this.lambdaQuery()
                .like(StringUtils.hasText(query.getName()), WarehouseEntity::getName, query.getName())
                .eq(StringUtils.hasText(query.getCode()), WarehouseEntity::getCode, query.getCode())
                .eq(query.getStatus() != null, WarehouseEntity::getStatus, query.getStatus())
                .orderByDesc(WarehouseEntity::getCreateTime)
                .page(page);

        List<WarehouseEntity> records = warehousePage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new Page<>(query.getPageNum(), query.getPageSize(), 0);
        }

        // 2. 批量查询负责人名称，彻底杜绝 N+1
        Set<Long> managerIds = records.stream()
                .map(WarehouseEntity::getManagerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> userMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(managerIds)) {
            userMap = userService.listByIds(managerIds).stream()
                    .collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));
        }

        // 3. 内存组装 VO 列表
        final Map<Long, String> finalUserMap = userMap;
        List<WarehouseVO> voList = records.stream().map(warehouse -> {
            WarehouseVO vo = new WarehouseVO();
            BeanUtils.copyProperties(warehouse, vo);
            if (warehouse.getManagerId() != null) {
                vo.setManagerName(finalUserMap.get(warehouse.getManagerId()));
            }
            return vo;
        }).collect(Collectors.toList());

        Page<WarehouseVO> voPage = new Page<>(warehousePage.getCurrent(), warehousePage.getSize(), warehousePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}
