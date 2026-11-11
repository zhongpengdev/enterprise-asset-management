package com.example.assetperform.modules.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assetperform.common.result.Result;
import com.example.assetperform.modules.domain.dto.UserCreateDTO;
import com.example.assetperform.modules.domain.dto.UserQueryDTO;
import com.example.assetperform.modules.domain.dto.UserRoleAssignDTO;
import com.example.assetperform.modules.domain.vo.UserVO;
import com.example.assetperform.modules.service.UserRoleService;
import com.example.assetperform.modules.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;

    // 新增员工
    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return Result.success();
    }

    // 分页查询员工列表，脱敏用户密码
    @GetMapping("/list")
    public Result<Page<UserVO>> list(UserQueryDTO queryDTO) {
        return Result.success(userService.listPage(queryDTO));
    }

    // 为用户分配角色
    @PostMapping("/assign-roles")
    public Result<Void> assignRole(@Valid @RequestBody UserRoleAssignDTO userRoleAssignDTO){
        userRoleService.assignRoles(userRoleAssignDTO);
        return Result.success();
    }
}
