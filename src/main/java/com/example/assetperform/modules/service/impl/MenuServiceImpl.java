package com.example.assetperform.modules.service.impl;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.common.security.LoginUser;
import com.example.assetperform.common.security.SecurityUtils;
import com.example.assetperform.modules.domain.entity.MenuEntity;
import com.example.assetperform.modules.domain.vo.MenuTreeVO;
import com.example.assetperform.modules.mapper.MenuMapper;
import com.example.assetperform.modules.service.MenuService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, MenuEntity> implements MenuService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // 后面动态拼接userId
    public static final String USER_MENU_KEY_PREFIX = "sys:menu:user:";


    @Override
    public List<MenuTreeVO> getCurrentUserMenuTree() {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登陆");
        }

        String cacheKey = USER_MENU_KEY_PREFIX + userId;

        try {
            String menuJson = stringRedisTemplate.opsForValue().get(cacheKey);

            if (StringUtils.hasText(menuJson)) {
                return objectMapper.readValue(menuJson, new TypeReference<List<MenuTreeVO>>() {
                });
            }
        } catch (Exception e) {
            // 自动降级
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();
        boolean isAdmin = loginUser != null &&
                loginUser.getRoles() != null &&
                loginUser.getRoles().contains("ROLE_ADMIN"); // 是超级管理员

        List<MenuEntity> menuList;

        if (isAdmin) {
            // 将权限表的所有功能都返回
            menuList = this.lambdaQuery()
                    .eq(MenuEntity::getStatus, 0)
                    .orderByAsc(MenuEntity::getSort)
                    .list();
        } else {
            menuList = this.baseMapper.selectMenusByUserId(userId);
        }

        if (CollectionUtils.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        // 转成VO
        List<MenuTreeVO> voList = menuList.stream().map(m -> {
            MenuTreeVO vo = new MenuTreeVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).toList();

        // 3. 高性能线性建树：按 parentId 分组后挂载子节点
        Map<Long, List<MenuTreeVO>> parentGroup = voList.stream()
                .collect(Collectors.groupingBy(MenuTreeVO::getParentId));
        voList.forEach(node -> node.setChildren(parentGroup.getOrDefault(node.getId(), Collections.emptyList())));

        // 4. 返回顶级目录（parentId 为 0 的节点）
        List<MenuTreeVO> list = voList.stream()
                .filter(node -> Long.valueOf(0L).equals(node.getParentId()))
                .collect(Collectors.toList());

        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(list),
                    Duration.ofHours(1)
            );
        } catch (Exception e) {
            // 写入失败不阻断返回
        }
        return list;

    }
}
