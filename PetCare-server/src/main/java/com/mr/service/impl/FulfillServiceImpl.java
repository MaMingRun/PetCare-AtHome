package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.dto.CheckinDTO;
import com.mr.entity.*;
import com.mr.enums.OrderStatus;
import com.mr.mapper.*;
import com.mr.service.FulfillService;
import com.mr.vo.ProviderOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.mr.dto.RecordCreateDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service
public class FulfillServiceImpl implements FulfillService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDayMapper orderDayMapper;
    @Autowired
    private OrderStatusLogMapper orderStatusLogMapper;
    @Autowired
    private ServiceRecordMapper serviceRecordMapper;
    @Autowired
    private OrderAlertMapper alertMapper;
    @Autowired
    private OrderPetMapper orderPetMapper;

    @Override
    public List<ProviderOrderVO> listProviderOrders(Long providerId, String status) {
        return orderMapper.listByProvider(providerId, status)
                .stream().map(this::toProviderVO).collect(Collectors.toList());
    }

    @Override
    public ProviderOrderVO getProviderOrderDetail(Long orderId, Long providerId) {
        Orders o = orderMapper.getByIdAndProvider(orderId, providerId);
        if (o == null) {
            throw new BaseException("订单不存在");
        }
        ProviderOrderVO v = toProviderVO(o);
        v.setDayList(orderDayMapper.listByOrderId(orderId));
        return v;
    }

    private ProviderOrderVO toProviderVO(Orders o) {
        ProviderOrderVO v = new ProviderOrderVO();
        v.setId(o.getId());
        v.setOrderNo(o.getOrderNo());
        v.setServiceName(o.getServiceName());
        v.setStatus(o.getStatus());
        v.setStartDate(o.getStartDate());
        v.setDays(o.getDays());
        v.setTimeSlot(o.getTimeSlot());
        v.setRemark(o.getRemark());
        v.setContactName(o.getContactName());
        v.setContactPhone(o.getContactPhone());
        StringBuilder sb = new StringBuilder();
        if (o.getCity() != null) sb.append(o.getCity());
        if (o.getDistrict() != null) sb.append(o.getDistrict());
        if (o.getCommunity() != null) sb.append(o.getCommunity());
        if (o.getDetail() != null) sb.append(o.getDetail());
        v.setAddress(sb.toString());
        v.setEntryMode(o.getEntryMode());
        v.setLongitude(o.getLongitude());
        v.setLatitude(o.getLatitude());
        v.setPayAmount(o.getPayAmount());
        v.setCreatedAt(o.getCreatedAt());
        v.setPetNames(orderPetMapper.listByOrderId(o.getId())
                .stream().map(OrderPet::getPetName).collect(Collectors.toList()));
        return v;
    }

    @Override
    @Transactional
    public void depart(Long orderId, Long providerId) {
        advance(orderId, providerId, OrderStatus.ACCEPTED, OrderStatus.DEPARTED, "服务师出发");
    }

    @Override
    @Transactional
    public void arrive(Long orderId, Long providerId, CheckinDTO dto) {
        if (dto.getLongitude() == null || dto.getLatitude() == null) {
            throw new BaseException("打卡需要上传定位");
        }
        advance(orderId, providerId, OrderStatus.DEPARTED, OrderStatus.ARRIVED, "服务师到达");

        //打卡留痕：算与订单地址快照坐标的偏差，>500m 标记告警
        Orders order = orderMapper.getById(orderId);
        int distance = distanceMeters(order.getLongitude(), order.getLatitude(),
                dto.getLongitude(), dto.getLatitude());
        OrderDay day = requireTodayDay(orderId);

        ServiceRecord r = new ServiceRecord();
        r.setOrderId(orderId);
        r.setDayId(day.getId());
        r.setProviderId(providerId);
        r.setType("CHECKIN");
        r.setLongitude(dto.getLongitude());
        r.setLatitude(dto.getLatitude());
        r.setDistanceMeters(distance);
        r.setDeviationAlert(distance > 500 ? 1 : 0);
        if (distance > 500) {
            // 注：OrderAlertMapper 需 @Autowired 进来
            if (alertMapper.countUnHandledByType(orderId, "DEVIATION") == 0) {
                OrderAlert a = new OrderAlert();
                a.setOrderId(orderId);
                a.setAlertType("DEVIATION");
                a.setLevel("HIGH");
                a.setAlertText("服务师打卡坐标偏离地址 " + distance + " 米，疑似位置造假");
                alertMapper.insert(a);
            }
        }
        r.setWatermarked(0);
        r.setCapturedAt(LocalDateTime.now());
        serviceRecordMapper.insert(r);
    }

    @Override
    @Transactional
    public void start(Long orderId, Long providerId) {
        //先联动当天子单：必须在服务日当天且子单还是 PENDING
        OrderDay day = requireTodayDay(orderId);
        if (orderDayMapper.markStatus(day.getId(), "PENDING", "SERVING") == 0) {
            throw new BaseException("当天服务已开始或已完成");
        }
        advance(orderId, providerId, OrderStatus.ARRIVED, OrderStatus.SERVING, "开始服务");
    }

    @Override
    @Transactional
    public void finishDay(Long orderId, Long providerId) {
        //1. 当天子单 SERVING→DONE
        OrderDay day = requireTodayDay(orderId);
        if (orderDayMapper.markStatus(day.getId(), "SERVING", "DONE") == 0) {
            throw new BaseException("当天服务未在进行中");
        }

        //2. 还有未完成的天 → 回 ACCEPTED 等下一天；全部完成 → PENDING_CONFIRM
        boolean allDone = orderDayMapper.countNotDone(orderId) == 0;
        OrderStatus to = allDone ? OrderStatus.PENDING_CONFIRM : OrderStatus.ACCEPTED;
        advance(orderId, providerId, OrderStatus.SERVING, to,
                allDone ? "全部服务完成，待用户确认" : "当天服务完成");
    }


    @Override
    @Transactional
    public void addRecord(Long orderId, Long providerId, RecordCreateDTO dto) {
        //1. 必须是我的单且在服务中
        Orders order = orderMapper.getById(orderId);
        if (order == null || !providerId.equals(order.getProviderId())) {
            throw new BaseException("订单不存在");
        }
        if (!OrderStatus.SERVING.name().equals(order.getStatus())) {
            throw new BaseException("服务未开始，不能留痕");
        }

        //2. 类型与内容校验
        if (!Set.of("PHOTO", "VIDEO", "NOTE").contains(dto.getType())) {
            throw new BaseException("不支持的记录类型");
        }
        if (("PHOTO".equals(dto.getType()) || "VIDEO".equals(dto.getType()))
                && (dto.getMediaUrl() == null || dto.getMediaUrl().isBlank())) {
            throw new BaseException("请上传媒体文件");
        }
        if ("NOTE".equals(dto.getType()) && (dto.getRemark() == null || dto.getRemark().isBlank())) {
            throw new BaseException("请填写文字内容");
        }

        //3. 写记录，挂当天子单
        OrderDay day = requireTodayDay(orderId);
        ServiceRecord r = new ServiceRecord();
        r.setOrderId(orderId);
        r.setDayId(day.getId());
        r.setProviderId(providerId);
        r.setType(dto.getType());
        r.setMediaUrl(dto.getMediaUrl());
        r.setRemark(dto.getRemark());
        r.setWatermarked(0);
        r.setDeviationAlert(0);
        r.setCapturedAt(LocalDateTime.now());
        serviceRecordMapper.insert(r);
    }

    // ---------- 私有方法 ----------

    //条件更新主订单状态：前态不符或不是自己的单，影响行数=0
    private void advance(Long orderId, Long providerId,
                         OrderStatus from, OrderStatus to, String remark) {
        int rows = orderMapper.updateStatus(orderId, from.name(), to.name(), providerId);
        if (rows == 0) {
            throw new BaseException("订单状态已变化，请刷新");
        }
        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(orderId);
        slog.setFromStatus(from.name());
        slog.setToStatus(to.name());
        slog.setOperatorType("PROVIDER");
        slog.setOperatorId(providerId);
        slog.setRemark(remark);
        orderStatusLogMapper.insert(slog);
    }

    //取当天子单，不存在说明今天不是服务日
    private OrderDay requireTodayDay(Long orderId) {
        OrderDay day = orderDayMapper.getByOrderAndDate(orderId, LocalDate.now());
        if (day == null) {
            throw new BaseException("今天不是该订单的服务日");
        }
        return day;
    }





    //Haversine 公式：两个经纬度坐标之间的球面距离（米）
    private static int distanceMeters(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1.doubleValue()) - Math.toRadians(lng2.doubleValue());
        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2)
                        + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        return (int) Math.round(s * 6371000); //地球半径 6371km
    }
}
