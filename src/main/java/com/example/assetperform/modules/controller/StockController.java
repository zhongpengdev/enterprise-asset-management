package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.StockInDTO;
import com.example.assetperform.modules.domain.dto.StockQueryDTO;
import com.example.assetperform.modules.domain.dto.StockRecordQueryDTO;
import com.example.assetperform.modules.domain.vo.StockRecordVO;
import com.example.assetperform.modules.domain.vo.StockVO;
import com.example.assetperform.modules.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // 1. 物资入库（支持原子累计与流水自动生成）
    @PostMapping("/in")
    public Result<Void> stockIn(@Valid @RequestBody StockInDTO dto) {
        stockService.stockIn(dto);
        return Result.success();
    }

    // 2. 分页多条件查询库存（含物理库存、预占库存、可用库存及安全库存预警）
    @GetMapping("/list")
    public Result<Page<StockVO>> list(StockQueryDTO query) {
        return Result.success(stockService.listPage(query));
    }

    // 3. 分页查询库存变动历史流水
    @GetMapping("/records")
    public Result<Page<StockRecordVO>> listRecords(StockRecordQueryDTO query) {
        return Result.success(stockService.listRecords(query));
    }
}
