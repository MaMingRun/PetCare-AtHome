package com.mr.service;

public interface ConfirmService {

    /** 用户确认收货：PENDING_CONFIRM → SETTLING */
    void confirm(Long orderId, Long userId);
}
