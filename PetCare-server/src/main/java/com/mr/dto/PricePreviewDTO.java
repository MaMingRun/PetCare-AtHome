package com.mr.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PricePreviewDTO {

    private Long serviceId;          //服务类目
    private List<Long> petIds;       //选中的宠物
    private Integer days;            //连续天数
    private BigDecimal distanceKm;   //距离（dev 手填，以后由坐标计算）
}