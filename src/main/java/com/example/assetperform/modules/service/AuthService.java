package com.example.assetperform.modules.service;

import com.example.assetperform.modules.domain.dto.LoginDTO;
import com.example.assetperform.modules.domain.vo.LoginVO;

public interface AuthService{
    LoginVO login(LoginDTO loginDTO);
}
