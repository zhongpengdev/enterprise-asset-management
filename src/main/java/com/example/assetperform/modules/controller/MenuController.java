package com.example.assetperform.modules.controller;

import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.vo.MenuTreeVO;
import com.example.assetperform.modules.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 获取当前登录用户拥有的权限菜单树
    @GetMapping("/user-menus")
    public Result<List<MenuTreeVO>> getUserMenus() {
        return Result.success(menuService.getCurrentUserMenuTree());
    }
}