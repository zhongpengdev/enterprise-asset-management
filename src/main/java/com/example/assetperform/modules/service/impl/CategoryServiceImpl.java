package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.modules.domain.dto.CategoryCreateDTO;
import com.example.assetperform.modules.domain.entity.CategoryEntity;
import com.example.assetperform.modules.domain.vo.CategoryTreeVO;
import com.example.assetperform.modules.mapper.CategoryMapper;
import com.example.assetperform.modules.service.CategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public void createCategory(CategoryCreateDTO dto) {
        
        // 1. 校验同级分类下名称是否重复
        boolean exist = this.lambdaQuery()
                .eq(CategoryEntity::getParentId, dto.getParentId())
                .eq(CategoryEntity::getName, dto.getName())
                .exists();
        if (exist) {
            throw new BusinessException("该分类名称已存在！");
        }
        
        // 2. DTO 转 Entity 入库
        CategoryEntity categoryEntity = new CategoryEntity();
        BeanUtils.copyProperties(dto, categoryEntity);
        this.save(categoryEntity);
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 1. 查询所有正常状态的分类
        List<CategoryEntity> categoryList = this.lambdaQuery()
                .eq(CategoryEntity::getStatus, 0)
                .orderByAsc(CategoryEntity::getSort)
                .list();
        
                // 2. 转成 VO 列表
        List<CategoryTreeVO> voList = categoryList.stream().map(cat -> {
            CategoryTreeVO vo = new CategoryTreeVO();
            BeanUtils.copyProperties(cat, vo);
            return vo;
        }).toList();
        
        // 3. 按 parentId 分组并挂载 children
        Map<Long, List<CategoryTreeVO>> childrenMap = voList.stream()
                .collect(Collectors.groupingBy(CategoryTreeVO::getParentId));
        voList.forEach(node ->
                node.setChildren(childrenMap.getOrDefault(node.getId(), new ArrayList<>()))
        );
        
        // 4. 只返回顶级分类
        return voList.stream()
                .filter(node -> node.getParentId() == 0L)
                .toList();
    }

    @Override
    public void deleteCategory(Long id) {
        // 校验是否存在子分类
        boolean hasChildren = this.lambdaQuery()
                .eq(CategoryEntity::getParentId, id)
                .exists();
        if (hasChildren) {
            throw new BusinessException("该分类下存在子分类，无法删除！");
        }
        this.removeById(id);
    }
}
