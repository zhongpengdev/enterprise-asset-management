package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.DeviceQueryDTO;
import com.example.assetperform.modules.domain.dto.DeviceRegisterDTO;
import com.example.assetperform.modules.domain.entity.DeviceEntity;
import com.example.assetperform.modules.domain.vo.DeviceVO;

public interface DeviceService extends IService<DeviceEntity> {

    // 录入固定资产设备
    void registerDevice(DeviceRegisterDTO dto);

    // 分页多条件查询资产台账
    Page<DeviceVO> listPage(DeviceQueryDTO query);
}