package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockRecordQueryDTO extends PageQuery {

    private Long warehouseId;    // 仓库ID筛选

    private Long itemId;         // 物资ID筛选

    private Integer type;        // 变动类型（1-采购入库，2-申领出库，3-退库入库，4-盘点调整，5-报废出库）

    private String recordNo;     // 流水单号

    private String bizNo;        // 关联业务单据号
}
