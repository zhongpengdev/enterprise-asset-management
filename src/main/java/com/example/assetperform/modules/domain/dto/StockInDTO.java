package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockInDTO {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "物资档案ID不能为空")
    private Long itemId;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer num;

    private String bizNo;        // 关联业务单据号（如采购单号，选填）

    private String remark;       // 备注说明（选填）
}
