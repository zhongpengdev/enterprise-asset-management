package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_warehouse")
public class WarehouseEntity extends BaseEntity {

    private String name;         // 仓库名称（如：总部办公用品仓）

    private String code;         // 仓库编码（如：WH-HQ-01）

    private Long managerId;      // 仓库负责人ID（关联 sys_user.id）

    private String phone;        // 联系电话

    private String address;      // 所在地址

    private Integer status;      // 状态（0-正常，1-停用）
}