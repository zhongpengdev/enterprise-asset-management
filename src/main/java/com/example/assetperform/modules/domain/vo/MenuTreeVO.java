package com.example.assetperform.modules.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuTreeVO {

    private Long id;

    private Long parentId;

    private String name;         // 菜单/目录名称

    private Integer type;        // 类型（0-目录，1-菜单，2-按钮）

    private String path;         // 路由地址

    private String permission;   // 权限标识

    private String icon;         // 图标

    private Integer sort;        // 排序

    private List<MenuTreeVO> children; // 子菜单列表（树状核心）
}