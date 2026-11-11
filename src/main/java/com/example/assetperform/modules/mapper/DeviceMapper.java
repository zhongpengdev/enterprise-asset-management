package com.example.assetperform.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assetperform.modules.domain.entity.DeviceEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<DeviceEntity> {
}