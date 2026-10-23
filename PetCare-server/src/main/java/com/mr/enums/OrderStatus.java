package com.mr.enums;

/**
 * 订单状态机（PRD §4.2）
 */
public enum OrderStatus {

    PENDING_PAY,         //待支付
    PENDING_ACCEPT,      //已预约·待接单
    ACCEPTED,            //已接单
    DEPARTED,            //服务师已出发
    ARRIVED,             //已到达
    SERVING,             //服务中
    PENDING_CONFIRM,     //已完成·待确认
    SETTLING,            //已确认·待评价
    REVIEWED,            //已评价
    FINISHED,            //已完成·未评价（终态）
    CANCELLED,           //已取消
    REFUNDING,           //退款中
    REFUNDED,            //已退款
    DISPUTED;            //争议中

    /**
     * 判断是否终态（不可再流转）
     */
    public boolean isTerminal() {
        return this == FINISHED || this == REVIEWED
                || this == CANCELLED || this == REFUNDED;
    }
}
