package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class ApplyAuditDTO {
    
    @NotNull (message = "申领单ID不能为空")
    private Long applyId;

    @NotNull (message = "审批结果不能为空")
    private Boolean pass; // true 通过，false驳回

    private String auditRemark; // 审批意见（选填）
}
