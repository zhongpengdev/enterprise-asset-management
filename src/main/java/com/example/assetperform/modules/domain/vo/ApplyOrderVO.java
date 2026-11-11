package com.example.assetperform.modules.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplyOrderVO {

    private Long id;

    private String orderNo;          // 申领单号

    private Long applicantId;        // 申领人ID

    private String applicantName;    // 申领人姓名（界面直接显示）

    private Long deptId;             // 申领部门ID

    private String deptName;         // 申领部门名称

    private Integer status;          // 状态（0-待审批，1-待发放，2-已驳回，3-已完成，4-已取消）

    private String reason;           // 申领用途

    private Long auditUserId;        // 审批人ID

    private String auditUserName;    // 审批人姓名

    private LocalDateTime auditTime; // 审批时间

    private String auditRemark;      // 审批意见

    private LocalDateTime createTime;// 提交时间

    private List<ApplyItemVO> items; // 申领物资明细列表

    @Data
    public static class ApplyItemVO {

        private Long id;

        private Long warehouseId;

        private String warehouseName;// 仓库名称

        private Long itemId;

        private String itemName;     // 物资名称

        private String itemCode;     // 物资编码

        private String model;        // 规格型号

        private String unit;         // 计量单位

        private Integer applyNum;    // 申领数量

        private Integer actualNum;   // 实际发放数量
    }
}