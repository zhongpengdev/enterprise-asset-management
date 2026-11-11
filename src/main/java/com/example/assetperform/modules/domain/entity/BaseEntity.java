package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import java.time.LocalDateTime;

@Data 
public class BaseEntity {
    
    @TableId (type = IdType.AUTO)
    private Long id;
    
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 逻辑删除注解
    @TableLogic 
    private Integer isDeleted;
}
