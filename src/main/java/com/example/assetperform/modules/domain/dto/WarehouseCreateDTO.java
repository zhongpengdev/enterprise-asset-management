package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseCreateDTO {

    @NotBlank(message = "仓库名称不能为空")
    private String name;

    @NotBlank(message = "仓库编码不能为空（如：WH-HQ-01）")
    private String code;

    private Long managerId;      // 仓库负责人ID（选填）

    private String phone;        // 联系电话

    private String address;      // 所在地址

    private Integer status = 0;  // 状态（0-正常，1-停用）
}
