package com.example.assetperform.modules.service;

import java.util.List;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.entity.MenuEntity;
import com.example.assetperform.modules.domain.vo.MenuTreeVO;

public interface MenuService extends IService<MenuEntity>{
    
    // 获取当前登录人拥有的动态菜单树
    List<MenuTreeVO> getCurrentUserMenuTree();
}
