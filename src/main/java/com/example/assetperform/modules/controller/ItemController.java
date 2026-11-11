package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.ItemCreateDTO;
import com.example.assetperform.modules.domain.dto.ItemQueryDTO;
import com.example.assetperform.modules.domain.vo.ItemVO;
import com.example.assetperform.modules.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 新增物资Item
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ItemCreateDTO dto) {
        itemService.createItem(dto);
        return Result.success();
    }

    // 2. 分页多条件查询物资档案
    @GetMapping("/list")
    public Result<Page<ItemVO>> list(ItemQueryDTO query) {
        return Result.success(itemService.listPage(query));
    }
}