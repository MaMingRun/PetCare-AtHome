package com.mr.service;

import com.mr.dto.CheckinDTO;
import com.mr.dto.RecordCreateDTO;
import com.mr.vo.ProviderOrderVO;

import java.util.List;

public interface FulfillService {

    /** 我的服务订单列表（按状态过滤） */
    List<ProviderOrderVO> listProviderOrders(Long providerId, String status);

    /** 我的服务订单详情（归属校验在 SQL 内） */
    ProviderOrderVO getProviderOrderDetail(Long orderId, Long providerId);

    //出发
    void depart(Long orderId, Long providerId);

    //到达（打卡：上传定位，写 CHECKIN 留痕）
    void arrive(Long orderId, Long providerId, CheckinDTO dto);

    //开始服务（当天子单 PENDING→SERVING）
    void start(Long orderId, Long providerId);

    //完成当天（当天子单→DONE；主订单回 ACCEPTED 或进 PENDING_CONFIRM）
    void finishDay(Long orderId, Long providerId);

    //服务过程留痕（PHOTO/VIDEO/NOTE，仅 SERVING 状态）
    void addRecord(Long orderId, Long providerId, RecordCreateDTO dto);
}
