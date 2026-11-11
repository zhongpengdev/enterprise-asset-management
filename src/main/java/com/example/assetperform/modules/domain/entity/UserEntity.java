package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@TableName ("sys_user")
@EqualsAndHashCode (callSuper = true)
public class UserEntity extends BaseEntity{
    private String username;

    private String password;

    private String realName;

    private String employeeNo;

    private Long deptId;

    private String phone;

    private String email;

    // 0 正常 1 冻结 2 离职
    private Integer status;
    
}
