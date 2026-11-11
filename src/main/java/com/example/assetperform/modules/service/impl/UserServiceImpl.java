package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.modules.domain.dto.UserCreateDTO;
import com.example.assetperform.modules.domain.dto.UserQueryDTO;
import com.example.assetperform.modules.domain.entity.DeptEntity;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.vo.UserVO;
import com.example.assetperform.modules.mapper.UserMapper;
import com.example.assetperform.modules.service.DeptService;
import com.example.assetperform.modules.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private final DeptService deptService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserVO> listPage(UserQueryDTO query) {

        // 分页查询
        Page<UserEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 模糊搜索
        Page<UserEntity> userPage = this.lambdaQuery()
                .like(StringUtils.hasText(query.getUsername()), UserEntity::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getPhone()), UserEntity::getPhone, query.getPhone())
                .eq(query.getDeptId() != null, UserEntity::getDeptId, query.getDeptId())
                .eq(query.getStatus() != null, UserEntity::getStatus, query.getStatus())
                .orderByDesc(UserEntity::getCreateTime)
                .page(page);

        List<UserEntity> records = userPage.getRecords();
        if (records.isEmpty()) {
            Page<UserVO> emptyPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        // 2. 批量查出涉及的部门名称（一条 SQL 批量查，彻底杜绝 N+1 性能问题）
        Set<Long> deptIds = records.stream()
                .map(UserEntity::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> deptNameMap = deptService.listByIds(deptIds).stream()
                .collect(Collectors.toMap(DeptEntity::getId, DeptEntity::getName));

        // 3. 组装 UserVO（BeanUtils 拷贝同名字段，password 自然被丢弃）
        List<UserVO> voList = records.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setDeptName(deptNameMap.getOrDefault(user.getDeptId(), "未分配部门"));
            return vo;
        }).toList();

        // 4. 封装成 Page<UserVO> 返回
        Page<UserVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }



    @Override
    public void createUser(UserCreateDTO dto) {
        // 1. 检查同名账号
        boolean userNameExists = this.lambdaQuery()
                .eq(UserEntity::getUsername, dto.getUsername())
                .exists();
        if (userNameExists) {
            throw new BusinessException("用户名已经存在！");
        }

        // 2. 所选部门合法性校验
        DeptEntity deptEntity = deptService.getById(dto.getDeptId());
        if (deptEntity == null) {
            throw new BusinessException("所选部门不存在！");
        }

        // 3. 将 DTO 安全拷贝到 Entity
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(dto, userEntity);

        // 4. 密码加密（未填则默认 123456）
        String rawPassword = StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456";
        userEntity.setPassword(passwordEncoder.encode(rawPassword));

        // 5. 保存入库
        this.save(userEntity);
    }
}
