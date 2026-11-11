package com.example.assetperform.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assetperform.modules.domain.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity>{
    
}
