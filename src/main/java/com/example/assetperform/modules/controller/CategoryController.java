package com.example.assetperform.modules.controller;

import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.CategoryCreateDTO;
import com.example.assetperform.modules.domain.vo.CategoryTreeVO;
import com.example.assetperform.modules.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController 
@RequestMapping ("/asset/category")
@RequiredArgsConstructor 
public class CategoryController {
    
    private final CategoryService categoryService;
    
    // 1. 新增分类
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryCreateDTO dto) {
        categoryService.createCategory(dto);
        return Result.success();
    }
    
    // 2. 查询分类树形结构
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.getCategoryTree());
    }
    
    // 3. 删除分类
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
