package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName ("sys_dept")
@EqualsAndHashCode (callSuper = true)
public class DeptEntity extends BaseEntity {
    private String name;

    private Long parentId;

    // 部门负责人的ID
    private Long leaderId;

    private Integer sort; // 排序字段

    private Integer status; // 状态字段，0正常， 1停用
}
