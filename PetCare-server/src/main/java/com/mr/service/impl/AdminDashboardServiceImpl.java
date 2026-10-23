package com.mr.service.impl;

import com.mr.common.result.PageResult;
import com.mr.dto.PageQueryDTO;
import com.mr.entity.OrderPet;
import com.mr.entity.Orders;
import com.mr.entity.Provider;
import com.mr.entity.Withdrawal;
import com.mr.enums.OrderStatus;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.OrderPetMapper;
import com.mr.mapper.ProviderMapper;
import com.mr.mapper.SettlementMapper;
import com.mr.mapper.WithdrawalMapper;
import com.mr.service.AdminDashboardService;
import com.mr.vo.AdminOrderVO;
import com.mr.vo.AdminWithdrawalVO;
import com.mr.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderPetMapper orderPetMapper;
    @Autowired private ProviderMapper providerMapper;
    @Autowired private SettlementMapper settlementMapper;
    @Autowired private WithdrawalMapper withdrawalMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO v = new DashboardVO();
        v.setTotalOrders(orderMapper.countByStatus(null) + orderMapper.countToday() - 0L); // 复用
        v.setTotalOrders(orderMapper.countTotalOrders());                                   // 见下方新增
        v.setPaidOrders(orderMapper.countPaidAndAfter());                                   // 见下方新增
        v.setTodayOrders(orderMapper.countToday());
        v.setReviewedOrders(orderMapper.countByStatus(OrderStatus.REVIEWED.name())
                + orderMapper.countByStatus(OrderStatus.FINISHED.name()));
        v.setTotalGmv(orderMapper.sumPayAmountPaid());
        v.setPendingAccept(orderMapper.countByStatus(OrderStatus.PENDING_ACCEPT.name()));
        v.setPendingSettlement(settlementMapper.countPending());
        v.setPendingWithdrawal(withdrawalMapper.countPage("APPLIED"));
        v.setTotalProviders(providerMapper.countTotalActive());
        return v;
    }

    @Override
    public PageResult<AdminOrderVO> ordersPage(PageQueryDTO q) {
        int page = q.getPage() == null ? 1 : q.getPage();
        int size = q.getSize() == null ? 10 : q.getSize();
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        long total = orderMapper.countPage(q.getStatus(), q.getKeyword());
        List<Orders> list = orderMapper.listPage(q.getStatus(), q.getKeyword(), offset, size);
        return new PageResult<>(list.stream().map(this::toAdminVO).collect(Collectors.toList()),
                total, page, size);
    }

    @Override
    public PageResult<AdminWithdrawalVO> withdrawalsPage(PageQueryDTO q) {
        int page = q.getPage() == null ? 1 : q.getPage();
        int size = q.getSize() == null ? 10 : q.getSize();
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        long total = withdrawalMapper.countPage(q.getStatus());
        List<Withdrawal> list = withdrawalMapper.listPage(q.getStatus(), offset, size);
        return new PageResult<>(list.stream().map(this::toWithdrawVO).collect(Collectors.toList()),
                total, page, size);
    }

    @Override
    public AdminOrderVO orderDetail(Long id) {
        Orders o = orderMapper.getById(id);
        return o == null ? null : toAdminVO(o);
    }

    private AdminOrderVO toAdminVO(Orders o) {
        AdminOrderVO v = new AdminOrderVO();
        v.setId(o.getId());
        v.setOrderNo(o.getOrderNo());
        v.setServiceName(o.getServiceName());
        v.setStatus(o.getStatus());
        v.setStartDate(o.getStartDate());
        v.setDays(o.getDays());
        v.setTimeSlot(o.getTimeSlot());
        v.setContactName(o.getContactName());
        v.setContactPhone(o.getContactPhone());
        v.setAddress(catAddress(o));
        v.setEntryMode(o.getEntryMode());
        v.setPayAmount(o.getPayAmount());
        v.setRemark(o.getRemark());
        v.setCreatedAt(o.getCreatedAt());
        v.setPetNames(orderPetMapper.listByOrderId(o.getId())
                .stream().map(OrderPet::getPetName).collect(Collectors.toList()));
        if (o.getProviderId() != null) {
            Provider p = providerMapper.getById(o.getProviderId());
            if (p != null) v.setContactName(v.getContactName() + " (服务师:" + p.getRealName() + ")");
        }
        return v;
    }

    private AdminWithdrawalVO toWithdrawVO(Withdrawal w) {
        AdminWithdrawalVO v = new AdminWithdrawalVO();
        v.setId(w.getId());
        v.setWithdrawalNo(w.getWithdrawalNo());
        v.setProviderId(w.getProviderId());
        Provider p = providerMapper.getById(w.getProviderId());
        v.setProviderName(p == null ? null : p.getRealName());
        v.setBankCardMasked("---"); // 简化，不回传卡号
        v.setAmount(w.getAmount());
        v.setStatus(w.getStatus());
        v.setRejectReason(w.getRejectReason());
        v.setAppliedAt(w.getAppliedAt());
        v.setPaidAt(w.getPaidAt());
        return v;
    }

    private String catAddress(Orders o) {
        StringBuilder sb = new StringBuilder();
        if (o.getCity() != null) sb.append(o.getCity());
        if (o.getDistrict() != null) sb.append(o.getDistrict());
        if (o.getCommunity() != null) sb.append(o.getCommunity());
        if (o.getDetail() != null) sb.append(o.getDetail());
        return sb.toString();
    }
}
