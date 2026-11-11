package com.example.assetperform.modules.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCreateDTO {

    @NotBlank(message = "物资名称不能为空")
    private String name;

    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    private String model;

    @NotBlank(message = "计量单位不能为空")
    private String unit;

    private String brand;

    @NotNull(message = "参考单价不能为空")
    @DecimalMin(value = "0.00", message = "单价不能小于0")
    private BigDecimal price;

    @NotNull(message = "物资类型不能为空（1-消耗品，2-固定资产）")
    private Integer type;

    private Integer allowClaim = 1;

    private Integer safetyStock = 0;

    private String imageUrl;
}