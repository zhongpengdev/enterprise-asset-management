package com.example.assetperform.modules.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工分页查询
 * UserQueryDTO
 */
@Data 
@EqualsAndHashCode (callSuper = true)
public class UserQueryDTO extends PageQuery{ // 忘记继承PageQuery
    private String username; // 按照名字搜索，模糊查询

    private String phone;

    private Long deptId;

    private Integer status;
}
