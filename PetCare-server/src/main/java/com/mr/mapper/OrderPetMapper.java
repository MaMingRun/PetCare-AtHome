package com.mr.mapper;

import com.mr.entity.OrderPet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderPetMapper {
    void insert(OrderPet orderPet);

    //查询订单关联的宠物快照（管理端派单页展示）
    List<OrderPet> listByOrderId(@Param("orderId") Long orderId);
}