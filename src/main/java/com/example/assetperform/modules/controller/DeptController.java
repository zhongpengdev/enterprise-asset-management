package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.DeptCreateDTO;
import com.example.assetperform.modules.domain.dto.DeptQueryDTO;
import com.example.assetperform.modules.domain.dto.DeptUpdateDTO;
import com.example.assetperform.modules.domain.entity.DeptEntity;
import com.example.assetperform.modules.domain.vo.DeptTreeVO;
import com.example.assetperform.modules.service.DeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    // 1. 新增部门（参数校验 @Valid）
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DeptCreateDTO dto) {
        deptService.createDept(dto);
        return Result.success();
    }

    // 2. 修改部门（参数校验 @Valid）
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DeptUpdateDTO dto) {
        deptService.updateDept(dto);
        return Result.success();
    }

    // 3. 分页查询部门
    @GetMapping("/list")
    public Result<Page<DeptEntity>> list(DeptQueryDTO queryDTO) {
        return Result.success(deptService.listPage(queryDTO));
    }

    // 4. 获取部门树形结构
    @GetMapping("/tree")
    public Result<List<DeptTreeVO>> tree() {
        return Result.success(deptService.getDeptTree());
    }

    /**
     * 已经是Delete请求了直接跟id就行。
     * @param id
     * @return
     */
    // 删除部门
    @DeleteMapping ("{id}")
    public Result<Void> deleteDeptById(@PathVariable Long id){
        deptService.deleteDept(id);
        return Result.success();
    }
}
