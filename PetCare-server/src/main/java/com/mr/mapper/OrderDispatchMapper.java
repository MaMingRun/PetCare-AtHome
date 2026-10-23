package com.mr.mapper;

import com.mr.entity.OrderDispatch;
import com.mr.vo.ProviderDispatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDispatchMapper {

    // 新增一条派单记录（DISPATCH/ACCEPT/REJECT）
    void insert(OrderDispatch dispatch);

    // 查询某订单+某服务师最新一条派单记录（不限动作类型）
    OrderDispatch findLatestByOrderAndProvider(@Param("orderId") Long orderId,
                                               @Param("providerId") Long providerId);

    // 服务师端：待响应派单列表（最新记录是 DISPATCH 且未超时、订单仍待接单）
    List<ProviderDispatchVO> listPendingByProvider(@Param("providerId") Long providerId);

    //超时扫描：最新一条记录是 DISPATCH、已过响应截止、订单仍待接单的派单
    List<OrderDispatch> listExpiredPending();
}
