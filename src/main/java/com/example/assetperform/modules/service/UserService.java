package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.UserCreateDTO;
import com.example.assetperform.modules.domain.dto.UserQueryDTO;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.vo.UserVO;

public interface UserService extends IService<UserEntity> {
    // 创建员工（接收标准DTO）
    void createUser(UserCreateDTO dto);

    // 分页查询员工列表
    Page<UserVO> listPage(UserQueryDTO userQueryDTO);
}
