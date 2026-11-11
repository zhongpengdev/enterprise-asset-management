package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode (callSuper = true)
public class ApplyQueryDTO extends PageQuery{
    private String orderNo; // 申领单号

    private Integer status; // 状态（0-待审批，1-待发放，2-已驳回，3-已完成）
}
