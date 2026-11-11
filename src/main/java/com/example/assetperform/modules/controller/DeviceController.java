package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.common.security.SecurityUtils;
import com.example.assetperform.modules.domain.dto.DeviceQueryDTO;
import com.example.assetperform.modules.domain.dto.DeviceRegisterDTO;
import com.example.assetperform.modules.domain.dto.PageQuery;
import com.example.assetperform.modules.domain.vo.DeviceVO;
import com.example.assetperform.modules.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // 录入固定资产设备卡片
    @PostMapping
    public Result<Void> register(@Valid @RequestBody DeviceRegisterDTO dto) {
        deviceService.registerDevice(dto);
        return Result.success();
    }

    // 资产台账分页查询（管理员/行政视角）
    @GetMapping("/list")
    public Result<Page<DeviceVO>> list(DeviceQueryDTO query) {
        return Result.success(deviceService.listPage(query));
    }

    // 查询当前登录人名下的个人资产（员工视角，直接从 Token 识别登录人）
    @GetMapping("/my")
    public Result<Page<DeviceVO>> myDevices(PageQuery pageQuery) {
        DeviceQueryDTO query = new DeviceQueryDTO();
        query.setPageNum(pageQuery.getPageNum());
        query.setPageSize(pageQuery.getPageSize());
        query.setUserId(SecurityUtils.getUserId()); // 由后端自动取，前端无法伪造
        return Result.success(deviceService.listPage(query));
    }
}