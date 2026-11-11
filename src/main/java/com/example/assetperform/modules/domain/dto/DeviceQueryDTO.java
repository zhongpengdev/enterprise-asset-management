package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceQueryDTO extends PageQuery {

    private String assetCode;        // 资产编号查询

    private String serialNo;         // 出厂SN码查询

    private Long itemId;             // 按物资型号筛选

    private Long userId;             // 按使用责任人筛选

    private Long deptId;             // 按部门筛选

    private Integer status;          // 状态（0-闲置在库，1-使用中，2-待维修，3-已报废）
}