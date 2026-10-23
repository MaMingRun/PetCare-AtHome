package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.OrderDispatch;
import com.mr.entity.OrderStatusLog;
import com.mr.entity.Orders;
import com.mr.entity.Provider;
import com.mr.enums.OrderStatus;
import com.mr.mapper.OrderDispatchMapper;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.OrderPetMapper;
import com.mr.mapper.OrderStatusLogMapper;
import com.mr.mapper.ProviderMapper;
import com.mr.service.DispatchService;
import com.mr.vo.AdminOrderVO;
import com.mr.vo.ProviderDispatchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service   //全限定名，避让实体 Service
public class DispatchServiceImpl implements DispatchService {

    //派单动作枚举值（order_dispatch.action）
    private static final String ACT_DISPATCH = "DISPATCH";
    private static final String ACT_ACCEPT = "ACCEPT";
    private static final String ACT_REJECT = "REJECT";

    //PRD：指派后 15 分钟内服务师必须响应
    private static final int RESPONSE_MINUTES = 15;

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDispatchMapper orderDispatchMapper;
    @Autowired private OrderStatusLogMapper orderStatusLogMapper;
    @Autowired private ProviderMapper providerMapper;
    @Autowired private OrderPetMapper orderPetMapper;

    @Override
    public List<AdminOrderVO> listPendingAccept() {
        return orderMapper.listByStatus(OrderStatus.PENDING_ACCEPT.name())
                .stream().map(this::toAdminOrderVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void dispatch(Long orderId, Long providerId, Long adminId, String remark) {
        //先锁订单行
        orderMapper.lockById(orderId);

        //订单必须存在且处于待接单
        Orders order = orderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!OrderStatus.PENDING_ACCEPT.name().equals(order.getStatus())) {
            throw new BaseException("订单当前状态不可派单");
        }

        //服务师必须存在且在正常接单状态
        Provider provider = requireProvider(providerId);

        //该服务师对此订单最新一条记录还是派单 = 已派过未处理，防重复
        OrderDispatch latest = orderDispatchMapper.findLatestByOrderAndProvider(orderId, providerId);
        if (latest != null && ACT_DISPATCH.equals(latest.getAction())) {
            throw new BaseException("该服务师已有待响应的派单");
        }

        //写派单记录，15 分钟响应截止
        OrderDispatch d = new OrderDispatch();
        d.setOrderId(orderId);
        d.setProviderId(providerId);
        d.setAction(ACT_DISPATCH);
        d.setRemark(remark);
        d.setOperatorId(adminId);
        d.setResponseDeadline(LocalDateTime.now().plusMinutes(RESPONSE_MINUTES));
        orderDispatchMapper.insert(d);
    }

    @Override
    public List<ProviderDispatchVO> listMine(Long providerId) {
        return orderDispatchMapper.listPendingByProvider(providerId);
    }

    @Override
    @Transactional
    public void accept(Long orderId, Long providerId) {
        //1. 服务师必须可接单
        requireProvider(providerId);

        //2. 必须存在派给我的有效派单且未超时
        OrderDispatch latest = orderDispatchMapper.findLatestByOrderAndProvider(orderId, providerId);
        if (latest == null || !ACT_DISPATCH.equals(latest.getAction())) {
            throw new BaseException("未收到该订单的派单");
        }
        if (latest.getResponseDeadline().isBefore(LocalDateTime.now())) {
            throw new BaseException("派单已超时失效，请联系管理员重新派单");
        }

        //3. 条件更新防并发抢单：不是待接单说明已被别人接走或已取消
        int rows = orderMapper.markAccepted(orderId,
                OrderStatus.PENDING_ACCEPT.name(), OrderStatus.ACCEPTED.name(), providerId);
        if (rows == 0) {
            throw new BaseException("订单已被处理，请刷新");
        }

        //4. 派单流水：ACCEPT（服务师端动作，operator_id 为空）
        OrderDispatch d = new OrderDispatch();
        d.setOrderId(orderId);
        d.setProviderId(providerId);
        d.setAction(ACT_ACCEPT);
        orderDispatchMapper.insert(d);

        //5. 状态流转日志
        insertStatusLog(orderId, OrderStatus.PENDING_ACCEPT, OrderStatus.ACCEPTED,
                "PROVIDER", providerId, "服务师接单");
    }

    @Override
    @Transactional
    public void reject(Long orderId, Long providerId, String remark) {
        //1. 必须存在派给我的有效派单且未超时
        OrderDispatch latest = orderDispatchMapper.findLatestByOrderAndProvider(orderId, providerId);
        if (latest == null || !ACT_DISPATCH.equals(latest.getAction())) {
            throw new BaseException("未收到该订单的派单");
        }
        if (latest.getResponseDeadline().isBefore(LocalDateTime.now())) {
            throw new BaseException("派单已超时失效，无需拒单");
        }
        if (remark == null || remark.isBlank()) {
            throw new BaseException("请填写拒单原因");
        }

        //2. 写拒单记录，订单保持待接单，可继续改派他人
        OrderDispatch d = new OrderDispatch();
        d.setOrderId(orderId);
        d.setProviderId(providerId);
        d.setAction(ACT_REJECT);
        d.setRemark(remark);
        orderDispatchMapper.insert(d);

        //3. 拒单风控计数
        providerMapper.incrementRejectCount(providerId);
    }

    // ---------- 私有方法 ----------

    private Provider requireProvider(Long providerId) {
        Provider provider = providerMapper.getById(providerId);
        if (provider == null) {
            throw new BaseException("服务师不存在");
        }
        if (!"ACTIVE".equals(provider.getStatus())) {
            throw new BaseException("服务师当前不在接单状态");
        }
        return provider;
    }

    private void insertStatusLog(Long orderId, OrderStatus from, OrderStatus to,
                                 String operatorType, Long operatorId, String remark) {
        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(orderId);
        slog.setFromStatus(from.name());
        slog.setToStatus(to.name());
        slog.setOperatorType(operatorType);
        slog.setOperatorId(operatorId);
        slog.setRemark(remark);
        orderStatusLogMapper.insert(slog);
    }

    private AdminOrderVO toAdminOrderVO(Orders o) {
        AdminOrderVO vo = new AdminOrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setServiceName(o.getServiceName());
        vo.setStatus(o.getStatus());
        vo.setStartDate(o.getStartDate());
        vo.setDays(o.getDays());
        vo.setTimeSlot(o.getTimeSlot());
        vo.setContactName(o.getContactName());
        vo.setContactPhone(o.getContactPhone());
        vo.setAddress(o.getCity() + o.getDistrict() + o.getCommunity() + o.getDetail());
        vo.setEntryMode(o.getEntryMode());
        vo.setPayAmount(o.getPayAmount());
        vo.setRemark(o.getRemark());
        vo.setPetNames(orderPetMapper.listByOrderId(o.getId())
                .stream().map(op -> op.getPetName()).collect(Collectors.toList()));
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }
}
