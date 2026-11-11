package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockQueryDTO extends PageQuery {

    private Long warehouseId;    // 仓库ID筛选

    private Long itemId;         // 物资ID筛选

    private Boolean onlyWarning; // 是否仅查看库存预警数据
}
