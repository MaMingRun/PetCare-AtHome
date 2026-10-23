package com.mr.vo;
import lombok.Data;

@Data
public class DashboardVO {
    private long totalOrders;
    private long paidOrders;
    /** 今日新增订单数 */
    private long todayOrders;
    /** 已评价订单数 */
    private long reviewedOrders;
    /** 总 GMV（所有已支付订单 pay_amount 合计，单位分） */
    private long totalGmv;
    /** 待派单数（PENDING_ACCEPT） */
    private long pendingAccept;
    /** 待结算单数（settlement.status = PENDING） */
    private long pendingSettlement;
    /** 待处理提现单数（withdrawal.status = APPLIED） */
    private long pendingWithdrawal;
    /** 总入驻服务师数 */
    private long totalProviders;
}
