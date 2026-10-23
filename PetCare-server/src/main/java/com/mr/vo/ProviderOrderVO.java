package com.mr.vo;

import com.mr.entity.OrderDay;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务师端订单列表项 / 详情
 */
@Data
public class ProviderOrderVO {

    private Long id;
    private String orderNo;
    private String serviceName;
    private String status;
    private LocalDate startDate;
    private Integer days;
    private String timeSlot;
    private String remark;

    private String contactName;
    private String contactPhone;
    private String address;        // 拼接完整地址
    private String entryMode;

    /** 打卡定位参照：订单地址快照坐标 */
    private BigDecimal longitude;
    private BigDecimal latitude;

    private Integer payAmount;
    private LocalDateTime createdAt;

    private List<String> petNames;

    /** 按天排期（详情时填充） */
    private List<OrderDay> dayList;
}
