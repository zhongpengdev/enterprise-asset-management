package com.example.assetperform.modules.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.common.utils.JwtUtils;
import com.example.assetperform.modules.domain.dto.LoginDTO;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.vo.LoginVO;
import com.example.assetperform.modules.service.AuthService;
import com.example.assetperform.modules.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor 
public class AuthServiceImpl implements AuthService{
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // 登陆
    @Override 
    public LoginVO login(LoginDTO loginDTO){
        UserEntity user = userService.lambdaQuery()
                .eq(UserEntity::getUsername, loginDTO.getUsername())
                .one();

        if(user == null || !passwordEncoder.matches(loginDTO.getPassword(),  user.getPassword())){
            throw new BusinessException("用户名或密码错误");
        }

        // 账号业务状态校验
        if(user.getStatus() == 1) {
            throw new BusinessException("该账号已被管理员冻结，请联系管理员");
        }

        if(user.getStatus() == 2){
            throw new BusinessException("员工已经离职，账号已经停用");
        }

        // 签发token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 返回token给用户
        return new LoginVO(token, user.getUsername(), user.getRealName());
    }
}
