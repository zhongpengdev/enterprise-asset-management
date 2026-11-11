package com.example.assetperform.modules.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarehouseVO {

    private Long id;

    private String name;         // 仓库名称

    private String code;         // 仓库编码

    private Long managerId;      // 负责人ID

    private String managerName;  // 负责人姓名（前端直接展示）

    private String phone;        // 联系电话

    private String address;      // 所在地址

    private Integer status;      // 状态（0-正常，1-停用）

    private LocalDateTime createTime;
}
