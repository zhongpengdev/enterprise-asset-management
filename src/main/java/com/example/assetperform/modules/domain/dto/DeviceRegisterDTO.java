package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceRegisterDTO {

    @NotNull(message = "关联物资档案不能为空")
    private Long itemId;

    private String serialNo;         // 设备出厂SN码（如电脑机身序列号）

    private Long warehouseId;        // 所在仓库ID（选填）

    @NotNull(message = "实际采购金额不能为空")
    @DecimalMin(value = "0.00", message = "采购金额不能小于0")
    private BigDecimal purchasePrice;
}
