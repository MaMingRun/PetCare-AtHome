package com.mr.task;

import com.mr.entity.OrderStatusLog;
import com.mr.entity.Orders;
import com.mr.enums.OrderStatus;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.OrderStatusLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTimeoutTask {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderStatusLogMapper logMapper;

    /** 每 5 分钟扫描支付超时（pay_expire_at 通常是创建后 15 分钟，轮询频率要大于这个） */
    @Scheduled(cron = "0 */5 * * * *")
    public void handlePayTimeout() {
        List<Orders> list = orderMapper.listPayExpired();
        for (Orders o : list) {
            int rows = orderMapper.markPayCancelled(o.getId());
            if (rows > 0) {
                writeLog(o, OrderStatus.PENDING_PAY, OrderStatus.CANCELLED, "支付超时自动取消");
            }
        }
    }

    /** 每 10 分钟扫描派单超时（dispatch_deadline 通常是创建后 15 分钟） */
    @Scheduled(cron = "0 */10 * * * *")
    public void handleDispatchTimeout() {
        List<Orders> list = orderMapper.listDispatchExpired();
        for (Orders o : list) {
            int rows = orderMapper.markDispatchCancelled(o.getId());
            if (rows > 0) {
                writeLog(o, OrderStatus.PENDING_ACCEPT, OrderStatus.CANCELLED, "派单超时未响应，自动取消");
            }
        }
    }

    /** 每 30 分钟扫描确认超时（confirm_deadline = 完成后 3 天，低频即可） */
    @Scheduled(cron = "0 */30 * * * *")
    public void handleConfirmTimeout() {
        List<Orders> list = orderMapper.listConfirmExpired();
        for (Orders o : list) {
            int rows = orderMapper.markConfirmAutoSettling(o.getId());
            if (rows > 0) {
                writeLog(o, OrderStatus.PENDING_CONFIRM, OrderStatus.SETTLING, "确认超时，自动进入待评价");
            }
        }
    }

    /** 每小时扫描评价超时 → 自动 FINISHED */
    @Scheduled(cron = "0 0 * * * *")
    public void handleReviewTimeout() {
        // 没有专门的 list 查询，直接用一次多条件 update 搞定
        // SETTLING 且 review_deadline < now() → FINISHED
        int affected = orderMapper.batchReviewAutoFinished();  // ← 见下方补充
        // 注：写日志可以简化——只对实际更新的行写（复杂，简化版跳过或批量写）
    }

    // ---------- 辅助 ----------

    private void writeLog(Orders o, OrderStatus from, OrderStatus to, String remark) {
        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(o.getId());
        slog.setFromStatus(from.name());
        slog.setToStatus(to.name());
        slog.setOperatorType("SYSTEM");
        slog.setOperatorId(0L);
        slog.setRemark(remark);
        logMapper.insert(slog);
    }
}
