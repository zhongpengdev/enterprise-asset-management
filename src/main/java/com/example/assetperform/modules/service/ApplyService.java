package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.ApplyAuditDTO;
import com.example.assetperform.modules.domain.dto.ApplyCreateDTO;
import com.example.assetperform.modules.domain.dto.ApplyQueryDTO;
import com.example.assetperform.modules.domain.entity.ApplyOrderEntity;
import com.example.assetperform.modules.domain.vo.ApplyOrderVO;

/**
 * createApply：员工提交申领（涉及可用库存校验与预占库存累计）。

  
auditApply：主管审批（驳回则释放预占库存，通过则置为待发放）。

  
deliverApply：行政确认发放（真实扣减物理库存、释放预占、写入出库流水）。

  
listPage：分页查询申领单列表（支持前端表格展示）。
 * ApplyService
 */
public interface  ApplyService extends IService<ApplyOrderEntity>{
    
    
    // 员工提交申请
    /**
     * 用 SecurityUtils.getUserId() 获取当前登录人及所属部门。
循环检查物资可用库存是否足够（actual - locked >= applyNum），不够直接阻断。
原子预占库存：更新 locked_stock = locked + applyNum。
写入主表 apply_order（状态：0-待审批）和明细表 apply_item。
     * @param applyCreateDTO
     */
    void createApply(ApplyCreateDTO applyCreateDTO);

    // 主管审批

    /**
     * 驳回：单据置为 2-已驳回，并循环释放预占库存（locked - applyNum）。
通过：单据置为 1-待发放，预占库存保持不变。

     * @param applyAuditDTO
     */
    void auditApply(ApplyAuditDTO applyAuditDTO);



    // 行政确认发放（扣减库存，释放预占用，写出库流水）
    /**
     * 必须在 1-待发放 状态下执行。
真实出库：actual_stock - applyNum，同时释放预占 locked_stock - applyNum。
出库流水：向 wms_stock_record 写入 2-申领出库 审计记录（记录变动数量 -applyNum 与当前操作人）。
主表状态置为 3-已完成。
     * @param applyId
     */
    void deliverApply(Long applyId);



    // 分页查询申领单列表
    Page<ApplyOrderVO> listPage(ApplyQueryDTO applyQueryDTO);
}
