package com.example.assetperform.modules.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String employeeNo;
    private Long deptId;
    private String deptName;     // 所属部门名称（前端直接展示，无需二次查询）
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;

    // 不放pswd


}
