package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.ApplyAuditDTO;
import com.example.assetperform.modules.domain.dto.ApplyCreateDTO;
import com.example.assetperform.modules.domain.dto.ApplyQueryDTO;
import com.example.assetperform.modules.domain.vo.ApplyOrderVO;
import com.example.assetperform.modules.service.ApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;

    // 1. 员工提交申领单
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ApplyCreateDTO dto) {
        applyService.createApply(dto);
        return Result.success();
    }

    // 2. 主管审批（通过/驳回）
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody ApplyAuditDTO dto) {
        applyService.auditApply(dto);
        return Result.success();
    }

    // 3. 行政/仓管确认出库发放
    @PostMapping("/deliver/{id}")
    public Result<Void> deliver(@PathVariable("id") Long id) {
        applyService.deliverApply(id);
        return Result.success();
    }

    // 4. 分页查询申领单列表
    @GetMapping("/list")
    public Result<Page<ApplyOrderVO>> list(ApplyQueryDTO query) {
        return Result.success(applyService.listPage(query));
    }
}