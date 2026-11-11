package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.common.utils.CodeGenerator;
import com.example.assetperform.modules.domain.dto.ItemCreateDTO;
import com.example.assetperform.modules.domain.dto.ItemQueryDTO;
import com.example.assetperform.modules.domain.entity.CategoryEntity;
import com.example.assetperform.modules.domain.entity.ItemEntity;
import com.example.assetperform.modules.domain.vo.ItemVO;
import com.example.assetperform.modules.mapper.ItemMapper;
import com.example.assetperform.modules.service.CategoryService;
import com.example.assetperform.modules.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, ItemEntity> implements ItemService {

    private final CategoryService categoryService;

    // 注入redis编码生成器
    private final CodeGenerator codeGenerator;

    @Override
    public void createItem(ItemCreateDTO dto) {

        // 所属分类合法性核验（跨模块/跨表校验）
        CategoryEntity category = categoryService.getById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("所选物资分类不存在！");
        }

        // DTO 转 Entity 并保存
        ItemEntity itemEntity = new ItemEntity();
        BeanUtils.copyProperties(dto, itemEntity);

        // 生成唯一物资编码
        String itemCode = codeGenerator.generateCode("WZ");
        itemEntity.setCode(itemCode);

        // 存入库
        this.save(itemEntity);
    }

    @Override
    public Page<ItemVO> listPage(ItemQueryDTO query) {
        // 1. 物理分页查询物资表
        Page<ItemEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        Page<ItemEntity> itemPage = this.lambdaQuery()
                .like(StringUtils.hasText(query.getName()), ItemEntity::getName, query.getName())
                .eq(StringUtils.hasText(query.getCode()), ItemEntity::getCode, query.getCode())
                .eq(query.getCategoryId() != null, ItemEntity::getCategoryId, query.getCategoryId())
                .eq(query.getType() != null, ItemEntity::getType, query.getType())
                .eq(query.getStatus() != null, ItemEntity::getStatus, query.getStatus())
                .orderByDesc(ItemEntity::getCreateTime)
                .page(page);

        List<ItemEntity> records = itemPage.getRecords();
        if (records.isEmpty()) {
            Page<ItemVO> emptyPage = new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        // 2. 批量查出涉及的分类名称（彻底避免循环查库 N+1 问题）
        Set<Long> categoryIds = records.stream()
                .map(ItemEntity::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));

        // 3. 组装 ItemVO 列表
        List<ItemVO> voList = records.stream().map(item -> {
            ItemVO vo = new ItemVO();
            BeanUtils.copyProperties(item, vo);
            vo.setCategoryName(categoryNameMap.getOrDefault(item.getCategoryId(), "未分类"));
            return vo;
        }).toList();

        // 4. 封装分页返回
        Page<ItemVO> resultPage = new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
}