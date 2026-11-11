package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_stock_record")
public class StockRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;     // 流水单号（如 LS202611070001）

    private Long warehouseId;    // 仓库ID

    private Long itemId;         // 物资ID

    private Integer type;        // 变动类型（1-采购入库，2-申领出库，3-退库入库，4-盘点调整，5-报废出库）

    private Integer changeNum;   // 变动数量（正数为入库增加，负数为出库减少）

    private Integer beforeStock; // 变动前物理库存量

    private Integer afterStock;  // 变动后物理库存量

    private Long operatorId;     // 经办人ID

    private String bizNo;        // 关联业务单据号（如采购订单号、申领单号）

    private String remark;       // 备注说明

    private LocalDateTime createTime; // 变动发生时间
}
