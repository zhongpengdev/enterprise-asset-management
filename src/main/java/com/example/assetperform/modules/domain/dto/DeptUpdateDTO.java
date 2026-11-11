package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeptUpdateDTO {

    @NotNull(message = "部门ID不能为空")
    private Long id;

    @NotBlank(message = "部门名称不能为空")
    private String name;

    @NotNull(message = "父部门ID不能为空（顶级部门为0）")
    private Long parentId;

    private Long leaderId;

    private Integer sort = 0;

    private Integer status = 0;
}
