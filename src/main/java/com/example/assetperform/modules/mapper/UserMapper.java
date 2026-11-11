package com.example.assetperform.modules.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assetperform.modules.domain.entity.UserEntity;

@Mapper 
public interface UserMapper extends BaseMapper<UserEntity>{
    
}
