package com.example.assetperform.modules.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeviceVO {

    private Long id;

    private String assetCode;        // 资产编号（如 ZC202611070001）

    private Long itemId;

    private String itemName;         // 物资名称（如 MacBook Pro 14寸）

    private String brand;            // 品牌

    private String model;            // 规格型号

    private String serialNo;         // SN码

    private BigDecimal purchasePrice;// 采购金额

    private Integer status;          // 0-闲置在库，1-使用中，2-待维修，3-已报废

    private Long userId;

    private String userName;         // 当前使用人姓名（张三）

    private Long deptId;

    private String deptName;         // 所属部门名称（研发中心）

    private LocalDateTime claimTime; // 领用时间

    private LocalDateTime createTime;// 录入建档时间
}