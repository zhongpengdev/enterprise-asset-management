package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "父分类ID不能为空（顶级分类为0）")
    private Long parentId;

    private Integer sort = 0;

    private Integer status = 0;
}