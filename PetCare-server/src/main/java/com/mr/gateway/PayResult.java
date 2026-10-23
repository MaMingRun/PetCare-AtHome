package com.mr.gateway;

import lombok.Data;

/**
 * 支付结果（网关 → 业务的返回值）
 */
@Data
public class PayResult {

    private boolean success;
    private String transactionId;   //微信支付订单号
    private String message;

    public static PayResult success(String transactionId) {
        PayResult r = new PayResult();
        r.success = true;
        r.transactionId = transactionId;
        return r;
    }

    public static PayResult pending(String message) {
        PayResult r = new PayResult();
        r.success = false;
        r.message = message;
        return r;
    }
}
