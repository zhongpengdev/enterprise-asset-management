package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.WarehouseCreateDTO;
import com.example.assetperform.modules.domain.dto.WarehouseQueryDTO;
import com.example.assetperform.modules.domain.entity.WarehouseEntity;
import com.example.assetperform.modules.domain.vo.WarehouseVO;

public interface WarehouseService extends IService<WarehouseEntity> {

    // 1. 新增仓库
    void createWarehouse(WarehouseCreateDTO dto);

    // 2. 分页查询仓库列表（带负责人姓名）
    Page<WarehouseVO> listPage(WarehouseQueryDTO query);
}
