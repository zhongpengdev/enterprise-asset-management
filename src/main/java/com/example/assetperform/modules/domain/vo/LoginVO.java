package com.example.assetperform.modules.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class LoginVO {
    private String token;        // JWT 令牌字符串
    private String username;     // 当前登录用户名
    private String realName;     // 真实姓名
}
