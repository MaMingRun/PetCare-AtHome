package com.mr.service;

import com.mr.vo.AdminOrderVO;
import com.mr.vo.ProviderDispatchVO;

import java.util.List;

public interface DispatchService {

    //管理端：待派单订单池（支付完成待接单）
    List<AdminOrderVO> listPendingAccept();

    //管理端：指派服务师
    void dispatch(Long orderId, Long providerId, Long adminId, String remark);

    //服务师端：我的待响应派单
    List<ProviderDispatchVO> listMine(Long providerId);

    //服务师端：接单
    void accept(Long orderId, Long providerId);

    //服务师端：拒单
    void reject(Long orderId, Long providerId, String remark);
}
