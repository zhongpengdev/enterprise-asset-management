package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.ItemCreateDTO;
import com.example.assetperform.modules.domain.dto.ItemQueryDTO;
import com.example.assetperform.modules.domain.entity.ItemEntity;
import com.example.assetperform.modules.domain.vo.ItemVO;

public interface ItemService extends IService<ItemEntity> {

    // 1. 新增物资档案
    void createItem(ItemCreateDTO dto);

    // 2. 分页多条件查询物资档案（带分类名称）
    Page<ItemVO> listPage(ItemQueryDTO query);
}