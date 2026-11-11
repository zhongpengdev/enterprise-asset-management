package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode (callSuper = true)
@TableName ("asset_category")
public class CategoryEntity extends BaseEntity{
    private String name;       // 分类名称
    private Long parentId;     // 父分类ID（0表示顶级分类）
    private Integer sort;      // 排序权重
    private Integer status;    // 状态（0-正常，1-停用）
}
