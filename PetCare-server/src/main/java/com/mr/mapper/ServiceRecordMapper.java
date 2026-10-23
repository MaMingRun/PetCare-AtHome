package com.mr.mapper;

import com.mr.entity.ServiceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceRecordMapper {

    void insert(ServiceRecord record);

    //订单的全部留痕
    List<ServiceRecord> listByOrder(@Param("orderId") Long orderId);
}