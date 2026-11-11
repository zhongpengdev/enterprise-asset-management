package com.example.assetperform.modules.service;

import java.util.List;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.CategoryCreateDTO;
import com.example.assetperform.modules.domain.entity.CategoryEntity;
import com.example.assetperform.modules.domain.vo.CategoryTreeVO;

public interface CategoryService extends IService<CategoryEntity>{
    // 1. 新增分类
    void createCategory(CategoryCreateDTO dto);
    // 2. 获取分类树形结构
    List<CategoryTreeVO> getCategoryTree();
    // 3. 删除分类
    void deleteCategory(Long id);
    
}
