package com.example.assetperform.modules.domain.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer sort;

    private Integer status;

    private List<CategoryTreeVO> children = new ArrayList<>();
}