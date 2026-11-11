package com.example.assetperform.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assetperform.modules.domain.entity.ItemEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends BaseMapper<ItemEntity> {
}
