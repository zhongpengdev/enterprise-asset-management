package com.example.assetperform.modules.domain.dto;

import lombok.Data;

@Data 
public class PageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}
