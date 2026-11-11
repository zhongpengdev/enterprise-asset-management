package com.example.assetperform.modules.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class ApplyCreateDTO {
    @NotBlank (message = "申领用途不能为空")
    private String reason;

    @NotEmpty (message = "申领物资明细不能为空")
    @Valid 
    private List<ApplyItemParm> items;

    @Data 
    public static class ApplyItemParm {
        @NotNull(message = "出库仓库不能为空")
        private Long warehouseId;
        
        @NotNull(message = "物资不能为空")
        private Long itemId;
        
        @NotNull(message = "申领数量不能为空")
        @Min(value = 1, message = "申领数量必须大于0")
        private Integer applyNum;
        
    }

}
