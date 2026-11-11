package com.example.assetperform.modules.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ItemVO {

    private Long id;

    private String code;

    private String name;

    private Long categoryId;

    private String categoryName; // 分类名称（前端直接展示）

    private String model;

    private String unit;

    private String brand;

    private BigDecimal price;

    private Integer type;        // 1-消耗品，2-固定资产

    private Integer allowClaim;

    private Integer safetyStock;

    private Integer status;

    private LocalDateTime createTime;

    private String imageUrl; // 图片URL（前端直接展示）
}