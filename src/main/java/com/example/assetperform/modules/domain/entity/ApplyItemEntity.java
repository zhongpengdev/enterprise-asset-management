package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("apply_item")
public class ApplyItemEntity extends BaseEntity {

    private Long applyId;        // 关联主表ID（apply_order.id）

    private Long warehouseId;    // 出库仓库ID（关联 wms_warehouse.id）

    private Long itemId;         // 物资档案ID（关联 asset_item.id）

    private Integer applyNum;    // 申领数量

    private Integer actualNum;   // 实际发放数量
}