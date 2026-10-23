package com.mr.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CheckinDTO {
    private BigDecimal longitude;  // 打卡点经度
    private BigDecimal latitude;   // 打卡点纬度
}