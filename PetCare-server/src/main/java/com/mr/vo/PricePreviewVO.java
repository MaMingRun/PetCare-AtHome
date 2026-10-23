package com.mr.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
public class PricePreviewVO {

    private Integer baseAmount;      //基础费
    private Integer extraAmount;     //附加费合计
    private Integer discountAmount;  //优惠（暂为0）
    private Integer totalAmount;     //总价
    private List<FeeItem> items;     //费用明细（前端逐行展示）

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeItem {
        private String label;
        private Integer amount;
    }
}