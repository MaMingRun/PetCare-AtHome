package com.mr.mapper;

import com.mr.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderStatusLogMapper {

    // 新增一条订单状态变更日志
    void insert(OrderStatusLog log);

    /** 管理端：某订单的全部状态流转日志（时间升序） */
    List<OrderStatusLog> listByOrder(@Param("orderId") Long orderId);
}
