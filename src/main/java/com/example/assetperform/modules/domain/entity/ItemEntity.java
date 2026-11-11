package com.example.assetperform.modules.domain.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode(callSuper = true)
@TableName("asset_item")
public class ItemEntity extends BaseEntity {
    private String code;         // 物资编码（如 WZ2026110001，唯一）
    private String name;         // 物资名称
    private Long categoryId;     // 所属分类ID（关联 asset_category.id）
    private String model;        // 规格型号（如：14寸 M3/16G/512G）
    private String unit;         // 计量单位（如：台、包、支）
    private String brand;        // 品牌（如：苹果、得力）
    private BigDecimal price;    // 参考单价（注意：涉及金额必须使用 BigDecimal）
    private Integer type;        // 物资类型（1-消耗品，2-固定资产）
    private Integer allowClaim;  // 是否允许员工申领（0-否，1-是）
    private Integer safetyStock; // 安全库存阈值
    private Integer status;      // 状态（0-正常，1-停用）
    private String imageUrl;
}