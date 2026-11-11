package com.example.assetperform.modules.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.assetperform.config.RabbitConfig;
import com.example.assetperform.modules.domain.entity.ApplyItemEntity;
import com.example.assetperform.modules.domain.entity.ApplyOrderEntity;
import com.example.assetperform.modules.domain.entity.StockEntity;
import com.example.assetperform.modules.mapper.ApplyItemMapper;
import com.example.assetperform.modules.mapper.ApplyOrderMapper;
import com.example.assetperform.modules.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component 
@RequiredArgsConstructor 
public class ApplyTimeoutConsumer {

    private final ApplyOrderMapper applyOrderMapper;
    private final StockService stockService;
    private final ApplyItemMapper applyItemMapper;

    // 监听死信队列，处理超时未审批的申领单
    @RabbitListener(queues = RabbitConfig.APPLY_DLX_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleApplyTimeout(Long applyId) {
        log.info("处理超时未审批的申领单，applyId: {}", applyId);

        ApplyOrderEntity order = applyOrderMapper.selectById(applyId);

        if (order == null) {
            log.warn("申领单 [{}] 不存在，忽略消息", applyId);
            return;
        }

        // 幂等，如果状态时0则直接修改成状态4, 代表超时
        if (order.getStatus() != 0) {
            log.info("申领单 [{}] 当前状态为 [{}]，已无需超时处理，消息丢弃", applyId, order.getStatus());
            return;
        }

        // 修改订单状态
        order.setStatus(4);

        applyOrderMapper.updateById(order);

        // 查出该订单的所有的items并适当locked
        List<ApplyItemEntity> items = applyItemMapper.selectList(new LambdaQueryWrapper<ApplyItemEntity>().eq(ApplyItemEntity::getApplyId, applyId));

        for (ApplyItemEntity item : items) {
            // 回退locked_stock
            StockEntity stock = stockService.lambdaQuery()
                    .eq(StockEntity::getWarehouseId, item.getWarehouseId())
                    .eq(StockEntity::getItemId, item.getItemId())
                    .one();

            if (stock != null) {
                int locked = stock.getLockedStock() != null ? stock.getLockedStock() : 0;
                // 预占库存减去申请量，最低为 0
                stock.setLockedStock(Math.max(0, locked - item.getApplyNum()));
                stockService.updateById(stock);
                log.info("申领单 [{}] 超时作废，释放物资 [ID: {}] 预占库存: {}", applyId, item.getItemId(), item.getApplyNum());
            }
        }
        log.info("申领单 [{}] 超时自动作废并释放库存完成！", applyId);
    }
}
