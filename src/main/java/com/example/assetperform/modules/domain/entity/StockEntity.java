package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_stock")
public class StockEntity extends BaseEntity {

    private Long warehouseId;    // 所属仓库ID（关联 wms_warehouse.id）

    private Long itemId;         // 物资档案ID（关联 asset_item.id）

    private Integer actualStock; // 物理实际库存量

    private Integer lockedStock; // 申领预占库存量
}
