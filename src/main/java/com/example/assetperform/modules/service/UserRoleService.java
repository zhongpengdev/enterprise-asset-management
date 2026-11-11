package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.UserRoleAssignDTO;
import com.example.assetperform.modules.domain.entity.UserRoleEntity;

public interface UserRoleService extends IService<UserRoleEntity>{
    
    // 为员工分配角色
    void assignRoles(UserRoleAssignDTO userRoleAssignDTO);
}
