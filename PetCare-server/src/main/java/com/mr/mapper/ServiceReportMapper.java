package com.mr.mapper;

import com.mr.entity.ServiceReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ServiceReportMapper {

    void insert(ServiceReport report);

    /** 查某一天的报告（day_id 有 UNIQUE） */
    ServiceReport getByDayId(@Param("dayId") Long dayId);
}