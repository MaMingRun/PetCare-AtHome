package com.mr.service;

public interface WithdrawalService {
    Long apply(Long providerId, Integer amount, Long bankCardId);
    void approve(Long withdrawalId, Long adminId);
    void reject(Long withdrawalId, Long adminId, String reason);
    void markPaid(Long withdrawalId, String transferNo);
}
