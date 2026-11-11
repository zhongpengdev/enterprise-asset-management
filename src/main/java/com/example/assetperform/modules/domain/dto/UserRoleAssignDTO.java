package com.example.assetperform.modules.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data 
public class UserRoleAssignDTO {
    @NotNull (message = "用户ID不能为空")
    private Long userId;

    @NotNull (message = "用户角色列表不能为空")
    private List<Long> roleIds;
}
