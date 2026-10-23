package com.mr.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderCreateDTO {

    private Long serviceId;
    private Long addressId;
    private List<Long> petIds;
    private LocalDate startDate;     //ISO 格式 2026-10-20
    private Integer days;
    private String timeSlot;         //如 "14:00-15:00"
    private String remark;
    private BigDecimal distanceKm;
}
