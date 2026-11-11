package com.example.assetperform.modules.domain.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data 
public class DeptTreeVO {
    private Long id;
    private String name;
    private Long parentId;
    private Long leaderId;
    private Integer sort;
    private Integer status;

    // 子部门列表
    private List<DeptTreeVO> children = new ArrayList<>();
}
