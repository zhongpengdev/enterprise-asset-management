package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.StockInDTO;
import com.example.assetperform.modules.domain.dto.StockQueryDTO;
import com.example.assetperform.modules.domain.dto.StockRecordQueryDTO;
import com.example.assetperform.modules.domain.entity.StockEntity;
import com.example.assetperform.modules.domain.vo.StockRecordVO;
import com.example.assetperform.modules.domain.vo.StockVO;

public interface StockService extends IService<StockEntity> {

    // 1. 物资入库（支持原子累计与流水落库）
    void stockIn(StockInDTO dto);

    // 2. 分页多条件查询库存（支持可用库存计算与安全库存预警）
    Page<StockVO> listPage(StockQueryDTO query);

    // 3. 分页查询库存变动流水记录
    Page<StockRecordVO> listRecords(StockRecordQueryDTO query);
}
