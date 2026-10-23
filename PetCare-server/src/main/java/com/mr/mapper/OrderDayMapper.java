package com.mr.mapper;

import com.mr.entity.OrderDay;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface OrderDayMapper {

    void insert(OrderDay orderDay);

    //查某一天的子单（当天打卡用）
    OrderDay getByOrderAndDate(@Param("orderId") Long orderId, @Param("date") LocalDate date);

    //子单状态推进：条件更新，前态不对不动
    int markStatus(@Param("id") Long id, @Param("from") String from, @Param("to") String to);

    //还有几天没完成（决定主订单回 ACCEPTED 还是进 PENDING_CONFIRM）
    int countNotDone(@Param("orderId") Long orderId);

    //订单的全部按天子单（订单详情展示排期）
    List<OrderDay> listByOrderId(@Param("orderId") Long orderId);
}