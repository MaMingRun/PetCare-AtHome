package com.mr.vo;

import lombok.Data;

/**
 * 计价规则VO，订单结算页展示价格明细
 */
@Data
public class PriceRuleVO {

    /**
     * 计价规则类型
     * BASE：基础服务价
     * EXTRA_PET：额外宠物加价
     * WEIGHT：宠物重量加价
     * DISTANCE：距离远程加价
     */
    private String ruleType;

    /** 价格项显示名称（前端展示文本，如：基础服务费、额外猫咪加价） */
    private String label;

    /** 金额，单位：分，正数为加价 */
    private Integer amount;

    /** 规则配置JSON字符串，用于前端展示规则详情，例如{"maxCount":2}最多2只宠物 */
    private String ruleConfig;
}