package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.WarehouseCreateDTO;
import com.example.assetperform.modules.domain.dto.WarehouseQueryDTO;
import com.example.assetperform.modules.domain.vo.WarehouseVO;
import com.example.assetperform.modules.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // 1. 新增仓库
    @PostMapping
    public Result<Void> create(@Valid @RequestBody WarehouseCreateDTO dto) {
        warehouseService.createWarehouse(dto);
        return Result.success();
    }

    // 2. 分页条件查询仓库列表
    @GetMapping("/list")
    public Result<Page<WarehouseVO>> list(WarehouseQueryDTO query) {
        return Result.success(warehouseService.listPage(query));
    }
}
