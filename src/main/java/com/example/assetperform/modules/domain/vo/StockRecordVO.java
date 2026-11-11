package com.example.assetperform.modules.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockRecordVO {

    private Long id;

    private String recordNo;         // 流水单号

    private Long warehouseId;

    private String warehouseName;    // 仓库名称

    private Long itemId;

    private String itemName;         // 物资名称

    private String itemCode;         // 物资编码

    private Integer type;            // 变动类型（1-采购入库，2-申领出库，3-退库入库，4-盘点调整，5-报废出库）

    private String typeName;         // 变动类型中文

    private Integer changeNum;       // 变动数量

    private Integer beforeStock;     // 变动前物理库存

    private Integer afterStock;      // 变动后物理库存

    private Long operatorId;

    private String operatorName;     // 操作人姓名

    private String bizNo;            // 关联业务单号

    private String remark;           // 备注说明

    private LocalDateTime createTime;// 发生时间
}
