package com.example.assetperform.modules.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_device")
public class DeviceEntity extends BaseEntity {

    private String assetCode;        // 资产编号（如 ZC202611070001，系统唯一）

    private Long itemId;             // 对应物资档案ID（关联 asset_item.id）

    private String serialNo;         // 出厂SN码/序列号

    private Long warehouseId;        // 所在仓库ID（闲置时存放位置）

    private Long userId;             // 当前使用责任人ID（关联 sys_user.id，NULL为闲置）

    private Long deptId;             // 当前使用部门ID（关联 sys_dept.id）

    private BigDecimal purchasePrice;// 实际采购金额

    private LocalDateTime claimTime; // 领用出库时间

    private Integer status;          // 状态（0-闲置在库，1-使用中，2-待维修，3-已报废）
}