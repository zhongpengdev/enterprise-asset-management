package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色表
 * RoleEntity
 */
@Data 
@EqualsAndHashCode (callSuper = true)
@TableName ("sys_role")
public class RoleEntity extends BaseEntity {
    private String name; 

    private String code; // 权限标识， ROLE_DEPT_LEADER

    private Integer sort; // 显示排序

    private Integer status; //角色状态 0 1
}
