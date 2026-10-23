package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.dto.ReportCreateDTO;
import com.mr.entity.*;
import com.mr.enums.OrderStatus;
import com.mr.mapper.OrderAlertMapper;
import com.mr.mapper.OrderDayMapper;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.ServiceReportMapper;
import com.mr.service.ReportService;
import com.mr.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDayMapper orderDayMapper;
    @Autowired
    private ServiceReportMapper reportMapper;
    @Autowired
    private OrderAlertMapper alertMapper;

    private static final String ALERT_REPORT_ABNORMAL = "REPORT_ABNORMAL";
    private static final String ALERT_DEVIATION = "DEVIATION";

    @Override
    @Transactional
    public ReportVO submit(Long orderId, Long providerId, ReportCreateDTO dto) {
        // 1. 归属校验：必须是我的单，且订单已结束当天服务
        Orders order = orderMapper.getById(orderId);
        if (order == null || !providerId.equals(order.getProviderId())) {
            throw new BaseException("订单不存在");
        }
        // 允许提交的状态：SERVING 中或 PENDING_CONFIRM 或 SETTLING（给 24h 宽限期）
        String s = order.getStatus();
        if (!OrderStatus.SERVING.name().equals(s)
                && !OrderStatus.PENDING_CONFIRM.name().equals(s)
                && !OrderStatus.SETTLING.name().equals(s)) {
            throw new BaseException("订单尚未完成，不能提交服务报告");
        }

        // 2. 取当天子单，必须存在且已 DONE（或 SERVING 状态当天可先提交报告）
        OrderDay day = orderDayMapper.getByOrderAndDate(orderId, java.time.LocalDate.now());
        if (day == null) {
            throw new BaseException("今天不是该订单的服务日");
        }
        // 3. 同一天不能重复提交（day_id UNIQUE 兜底，这里先查给出友好提示）
        if (reportMapper.getByDayId(day.getId()) != null) {
            throw new BaseException("今天已经提交过报告");
        }

        // 4. 写报告
        ServiceReport r = new ServiceReport();
        r.setOrderId(orderId);
        r.setDayId(day.getId());
        r.setProviderId(providerId);
        r.setEating(dto.getEating());
        r.setDrinking(dto.getDrinking());
        r.setExcretion(dto.getExcretion());
        r.setSpirit(dto.getSpirit());
        r.setAbnormalities(dto.getAbnormalities());
        r.setPhotos(dto.getPhotos());
        r.setSummary(dto.getSummary());
        r.setSubmittedAt(LocalDateTime.now());
        reportMapper.insert(r);

        // 5. abnormalities 非空 → 写 REPORT_ABNORMAL 告警（去重）
        if (dto.getAbnormalities() != null && !dto.getAbnormalities().isBlank()) {
            if (alertMapper.countUnHandledByType(orderId, ALERT_REPORT_ABNORMAL) == 0) {
                OrderAlert a = new OrderAlert();
                a.setOrderId(orderId);
                a.setAlertType(ALERT_REPORT_ABNORMAL);
                a.setLevel("MEDIUM");
                a.setAlertText("服务师报告异常：" + truncate(dto.getAbnormalities(), 120));
                alertMapper.insert(a);
            }
        }

        // 6. 回填 deviation_alert（如果 arrive 时没触发告警，但偏差>500m，这里补一条）
        //    （实际 arrive 已写告警，这里留作双保险，跳过）

        // 7. 组装 VO 返回
        ReportVO vo = new ReportVO();
        vo.setId(r.getId());
        vo.setEating(r.getEating());
        vo.setDrinking(r.getDrinking());
        vo.setExcretion(r.getExcretion());
        vo.setSpirit(r.getSpirit());
        vo.setAbnormalities(r.getAbnormalities());
        vo.setPhotos(r.getPhotos());
        vo.setSummary(r.getSummary());
        vo.setSubmittedAt(r.getSubmittedAt());
        return vo;
    }

    private String truncate(String s, int len) {
        return s.length() > len ? s.substring(0, len) + "..." : s;
    }
}