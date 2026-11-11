package com.example.assetperform.modules.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assetperform.modules.domain.entity.MenuEntity;

@Mapper 
public interface MenuMapper extends BaseMapper<MenuEntity>{
        
    // 根据用户ID多表关联，查询其所有可见菜单（排除已停用和逻辑删除的数据）
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 0 AND m.is_deleted = 0 " +
            "ORDER BY m.sort ASC")
    List<MenuEntity> selectMenusByUserId(@Param("userId") Long userId);
}