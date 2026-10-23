package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.dto.ReviewCreateDTO;
import com.mr.entity.*;
import com.mr.enums.OrderStatus;
import com.mr.mapper.*;
import com.mr.service.UserReviewService;
import com.mr.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UserReviewServiceImpl implements UserReviewService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderStatusLogMapper logMapper;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private ProviderMapper providerMapper;
    @Autowired private SettlementMapper settlementMapper;
    @Autowired private SettlementRuleMapper ruleMapper;
    @Autowired private ProviderWalletMapper walletMapper;
    @Autowired private ProviderWalletFlowMapper flowMapper;

    @Override
    @Transactional
    public ReviewVO submit(Long orderId, Long userId, ReviewCreateDTO dto) {
        // 1. 归属 + 状态闸门
        Orders order = orderMapper.getByIdAndUser(orderId, userId);
        if (order == null) throw new BaseException("订单不存在");
        if (!OrderStatus.SETTLING.name().equals(order.getStatus())) {
            throw new BaseException("订单当前不可评价（需先确认收货）");
        }

        // 2. 评分校验
        if (!allValidScore(dto.getScoreProfessional(), dto.getScorePunctual(),
                dto.getScoreCommunication(), dto.getScoreCare())) {
            throw new BaseException("评分必须为 1-5 的整数");
        }

        // 3. 幂等
        if (reviewMapper.getByOrderId(orderId) != null) {
            throw new BaseException("订单已评价");
        }

        // 4. 写 review
        Review r = new Review();
        r.setOrderId(orderId);
        r.setUserId(userId);
        r.setProviderId(order.getProviderId());
        r.setScoreProfessional(dto.getScoreProfessional());
        r.setScorePunctual(dto.getScorePunctual());
        r.setScoreCommunication(dto.getScoreCommunication());
        r.setScoreCare(dto.getScoreCare());
        r.setContent(dto.getContent());
        r.setImages(dto.getImages());
        r.setAnonymous(dto.getAnonymous() == null ? 0 : dto.getAnonymous());
        reviewMapper.insert(r);

        // 5. 更新 provider 四项评分均值
        updateProviderRating(order.getProviderId(), dto);

        // 6. 状态流转 SETTLING → REVIEWED
        int rows = orderMapper.updateStatus(orderId,
                OrderStatus.SETTLING.name(),
                OrderStatus.REVIEWED.name(),
                order.getProviderId());
        if (rows == 0) throw new BaseException("订单状态已变化");

        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(orderId);
        slog.setFromStatus(OrderStatus.SETTLING.name());
        slog.setToStatus(OrderStatus.REVIEWED.name());
        slog.setOperatorType("USER");
        slog.setOperatorId(userId);
        slog.setRemark("用户评价完成");
        logMapper.insert(slog);

        // 7. 结算入库（PENDING）
        createSettlement(order);

        // 8. 返回
        ReviewVO vo = new ReviewVO();
        vo.setId(r.getId());
        vo.setScoreProfessional(r.getScoreProfessional());
        vo.setScorePunctual(r.getScorePunctual());
        vo.setScoreCommunication(r.getScoreCommunication());
        vo.setScoreCare(r.getScoreCare());
        vo.setContent(r.getContent());
        vo.setImages(r.getImages());
        vo.setAnonymous(r.getAnonymous());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private boolean allValidScore(Integer... scores) {
        for (Integer s : scores) if (s == null || s < 1 || s > 5) return false;
        return true;
    }

    private void updateProviderRating(Long providerId, ReviewCreateDTO dto) {
        Provider p = providerMapper.getById(providerId);
        if (p == null) return;
        int n = p.getServiceCount() == null ? 0 : p.getServiceCount();
        int newN = n + 1;

        BigDecimal curProf = p.getRatingProfessional() == null ? BigDecimal.ZERO : p.getRatingProfessional();
        BigDecimal curPunct = p.getRatingPunctual() == null ? BigDecimal.ZERO : p.getRatingPunctual();
        BigDecimal curComm = p.getRatingCommunication() == null ? BigDecimal.ZERO : p.getRatingCommunication();
        BigDecimal curCare = p.getRatingCare() == null ? BigDecimal.ZERO : p.getRatingCare();

        BigDecimal newProf = weighted(curProf, n, dto.getScoreProfessional(), newN);
        BigDecimal newPunct = weighted(curPunct, n, dto.getScorePunctual(), newN);
        BigDecimal newComm = weighted(curComm, n, dto.getScoreCommunication(), newN);
        BigDecimal newCare = weighted(curCare, n, dto.getScoreCare(), newN);
        BigDecimal avg = newProf.add(newPunct).add(newComm).add(newCare)
                .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);

        providerMapper.updateRating(providerId, avg, newProf, newPunct, newComm, newCare, newN);
    }

    private BigDecimal weighted(BigDecimal oldAvg, int oldN, int newScore, int totalN) {
        return oldAvg.multiply(BigDecimal.valueOf(oldN))
                .add(BigDecimal.valueOf(newScore))
                .divide(BigDecimal.valueOf(totalN), 2, RoundingMode.HALF_UP);
    }

    private void createSettlement(Orders order) {
        SettlementRule rule = ruleMapper.getByCity(order.getCity());
        if (rule == null) throw new BaseException("未找到结算规则");

        BigDecimal rate = rule.getCommissionRate();
        int orderAmount = order.getPayAmount() == null ? 0 : order.getPayAmount();
        int platformFee = new BigDecimal(orderAmount).multiply(rate)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        int netAmount = orderAmount - platformFee;

        Settlement s = new Settlement();
        s.setOrderId(order.getId());
        s.setProviderId(order.getProviderId());
        s.setOrderAmount(orderAmount);
        s.setPlatformFee(platformFee);
        s.setNetAmount(netAmount);
        settlementMapper.insert(s);

        ProviderWallet w = walletMapper.getById(order.getProviderId());
        if (w == null) throw new BaseException("服务师钱包不存在");
        int rows = walletMapper.addPending(order.getProviderId(), netAmount, w.getVersion());
        if (rows == 0) throw new BaseException("钱包更新冲突，请重试");

        ProviderWalletFlow flow = new ProviderWalletFlow();
        flow.setProviderId(order.getProviderId());
        flow.setBizType("INCOME_PENDING");
        flow.setAmount(netAmount);
        flow.setBalanceAfter(w.getPendingSettlement() + netAmount);
        flow.setBizId(s.getId());
        flow.setRemark("订单 #" + order.getId() + " 结算入库");
        flowMapper.insert(flow);
    }
}
