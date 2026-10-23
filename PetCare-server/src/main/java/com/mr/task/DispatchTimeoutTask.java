package com.mr.task;

import com.mr.entity.OrderDispatch;
import com.mr.mapper.OrderDispatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 派单超时扫描：15 分钟未响应的派单自动失效
 * 做法：给该（订单,服务师）补一条 TIMEOUT 记录，使"最新一条记录"不再是 DISPATCH
 * 订单状态不动（一直是 PENDING_ACCEPT，从未离开派单池）
 */
@Component
@Slf4j
public class DispatchTimeoutTask {

    @Autowired
    private OrderDispatchMapper orderDispatchMapper;

    //fixedDelay：上一轮跑完后间隔多久再跑（毫秒）
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void scan() {
        List<OrderDispatch> expired = orderDispatchMapper.listExpiredPending();
        if (expired.isEmpty()) {
            return;
        }
        for (OrderDispatch d : expired) {
            OrderDispatch timeout = new OrderDispatch();
            timeout.setOrderId(d.getOrderId());
            timeout.setProviderId(d.getProviderId());
            timeout.setAction("TIMEOUT");
            timeout.setRemark("15分钟未响应，派单自动失效");
            orderDispatchMapper.insert(timeout);
            log.info("[派单超时] 订单={} 服务师={} 派单失效", d.getOrderId(), d.getProviderId());
        }
    }
}