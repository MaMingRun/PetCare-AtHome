package com.mr.mapper;

import com.mr.entity.OrderAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderAlertMapper {

    void insert(OrderAlert alert);

    /** 同订单同类型是否已有未处理告警（用于去重） */
    int countUnHandledByType(@Param("orderId") Long orderId,
                             @Param("alertType") String alertType);
}
