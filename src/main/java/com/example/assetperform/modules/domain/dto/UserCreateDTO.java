package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 20, message = "账号长度必须在 3-20 位之间")
    private String username;

    private String password; // 选填，不传则使用系统默认密码 123456

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String employeeNo;

    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
