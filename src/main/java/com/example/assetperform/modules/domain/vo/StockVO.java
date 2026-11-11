package com.example.assetperform.modules.domain.vo;

import lombok.Data;

@Data
public class StockVO {

    private Long id;

    private Long warehouseId;

    private String warehouseName;    // 仓库名称

    private Long itemId;

    private String itemName;         // 物资名称

    private String itemCode;         // 物资编码

    private String model;            // 规格型号

    private String unit;             // 单位

    private Integer actualStock;     // 物理实际库存

    private Integer lockedStock;     // 申领预占库存

    private Integer availableStock;  // 可用库存 = actualStock - lockedStock

    private Integer safetyStock;     // 安全库存阈值

    private Boolean isWarning;       // 是否低于安全库存预警
}
