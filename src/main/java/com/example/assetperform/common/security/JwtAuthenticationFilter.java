package com.example.assetperform.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.assetperform.modules.domain.entity.RoleEntity;
import com.example.assetperform.modules.domain.entity.UserRoleEntity;
import com.example.assetperform.modules.mapper.RoleMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.assetperform.common.utils.JwtUtils;
import com.example.assetperform.modules.mapper.UserRoleMapper;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component 
@RequiredArgsConstructor 
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    private final UserRoleMapper userRoleMapper;

    private final RoleMapper roleMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头
        String authHeader = request.getHeader("Authorization");

        // 拿到token
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 校验并解析token信息
            Claims claims = jwtUtils.parseToken(token);
            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();

                // 查出员工所有的角色
                List<String> roleCodes = Collections.emptyList();
                List<UserRoleEntity> userRoles = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRoleEntity>().eq(UserRoleEntity::getUserId, userId)
                );

                if (!userRoles.isEmpty()) {
                    List<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).toList();
                    List<RoleEntity> roles = roleMapper.selectBatchIds(roleIds);
                    roleCodes = roles.stream().map(RoleEntity::getCode).toList();
                }

                //构造当前用户信息
                LoginUser loginUser = new LoginUser(userId, username, roleCodes);
                List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                // 5. 【核心】存入 SecurityContextHolder（也就是写入当前请求线程的 ThreadLocal）
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // 放行进入Controller
        filterChain.doFilter(request, response);
    }
}