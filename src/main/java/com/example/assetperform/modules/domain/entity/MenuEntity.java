package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class MenuEntity extends BaseEntity {

    private Long parentId;       // 父菜单ID（0表示顶级）

    private String name;         // 菜单名称

    private Integer type;        // 类型（0-目录，1-菜单，2-按钮）

    private String path;         // 路由地址（如 /system/user）

    private String component;    // 组件路径

    private String permission;   // 权限标识（如 asset:apply:list）

    private String icon;         // 图标

    private Integer sort;        // 排序

    private Integer status;      // 状态（0-正常，1-停用）
}