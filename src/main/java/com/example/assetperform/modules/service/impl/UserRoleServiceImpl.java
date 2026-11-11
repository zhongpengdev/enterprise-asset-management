package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.modules.domain.dto.UserRoleAssignDTO;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.entity.UserRoleEntity;
import com.example.assetperform.modules.mapper.UserMapper;
import com.example.assetperform.modules.mapper.UserRoleMapper;
import com.example.assetperform.modules.service.UserRoleService;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@AllArgsConstructor 
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRoleEntity> implements UserRoleService {

    // 注入UserMapper来判断用户是当否存在
    private final UserMapper userMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Transactional (rollbackFor = Exception.class)
    @Override 
    public void assignRoles(UserRoleAssignDTO userRoleAssignDTO){
        // 1. 检查用户是否存在
        UserEntity user = userMapper.selectById(userRoleAssignDTO.getUserId());

        if(user == null){
            throw new BusinessException("用户不存在！");
        }

        // 清除用户旧的关联角色做全量更新
        this.lambdaUpdate()
                .eq(UserRoleEntity::getUserId, userRoleAssignDTO.getUserId())
                .remove();

        // 批量保存新角色列表
        if(userRoleAssignDTO.getRoleIds() != null && !userRoleAssignDTO.getRoleIds().isEmpty()){
            List<UserRoleEntity> list = userRoleAssignDTO.getRoleIds().stream().map(roleId ->
                    {
                        UserRoleEntity userRoleEntity = new UserRoleEntity();
                        userRoleEntity.setUserId(userRoleAssignDTO.getUserId());
                        userRoleEntity.setRoleId(roleId);
                        return userRoleEntity;
                    }
            ).toList();

            this.saveBatch(list);
        }

        stringRedisTemplate.delete(MenuServiceImpl.USER_MENU_KEY_PREFIX + userRoleAssignDTO.getUserId());
    }
}  
