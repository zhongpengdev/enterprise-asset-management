package com.example.assetperform.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assetperform.modules.domain.entity.StockRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecordEntity> {
}
