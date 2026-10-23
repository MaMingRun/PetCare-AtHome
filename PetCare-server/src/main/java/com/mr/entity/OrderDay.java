package com.mr.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 多日订单子项（按天拆分）
 */
@Data
public class OrderDay {

    private Long id;                // 主键ID
    private Long orderId;           // 主订单ID，关联Orders表，1个主订单对应多条OrderDay
    private LocalDate date;         // 服务日期（哪一天上门）
    private LocalTime slotBegin;    // 当天服务时段开始时间
    private LocalTime slotEnd;      // 当天服务时段结束时间
    private String status;          // 当日服务状态枚举：PENDING待服务 / SERVING服务中 / DONE已完成 / PAUSED暂停 / CANCELLED已取消
    private Integer paused;         // 是否暂停当天服务：1=暂停这一天，0=正常执行
    private LocalDateTime createdAt;// 记录创建时间
    private LocalDateTime updatedAt;// 记录更新时间
}
