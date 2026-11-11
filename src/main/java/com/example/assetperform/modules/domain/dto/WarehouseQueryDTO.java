package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WarehouseQueryDTO extends PageQuery {

    private String name;         // 仓库名称查询

    private String code;         // 仓库编码查询

    private Integer status;      // 状态筛选
}
