package com.example.assetperform.modules.controller;

import com.example.assetperform.modules.domain.dto.LoginDTO;
import com.example.assetperform.modules.domain.vo.LoginVO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping ("/auth")
@RestController 
@RequiredArgsConstructor 
public class AuthController {
    private final AuthService authService;

    // 员工登陆
    @PostMapping ("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO){
        return Result.success(authService.login(loginDTO));
    }
}
