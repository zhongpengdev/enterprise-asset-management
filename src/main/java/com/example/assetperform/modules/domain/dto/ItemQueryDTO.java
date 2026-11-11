package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemQueryDTO extends PageQuery {

    private String name;         // 名称模糊查询

    private String code;         // 编码查询

    private Long categoryId;     // 按分类筛选

    private Integer type;        // 按类型筛选（1-消耗品，2-固定资产）

    private Integer status;      // 状态筛选
}
