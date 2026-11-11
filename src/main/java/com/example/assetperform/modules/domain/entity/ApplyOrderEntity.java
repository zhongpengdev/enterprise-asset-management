package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("apply_order")
public class ApplyOrderEntity extends BaseEntity {

    private String orderNo;          // 申领单号（如 SL202611070001）

    private Long applicantId;        // 申领人ID（关联 sys_user.id）

    private Long deptId;             // 申领部门ID（关联 sys_dept.id）

    private Integer status;          // 状态（0-待审批，1-待发放，2-已驳回，3-已完成，4-已取消）

    private String reason;           // 申领用途/原因说明

    private Long auditUserId;        // 审批人ID（关联 sys_user.id）

    private LocalDateTime auditTime; // 审批时间

    private String auditRemark;      // 审批意见/驳回原因
}