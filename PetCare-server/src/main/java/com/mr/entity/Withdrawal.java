package com.mr.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Withdrawal {
    private Long id;
    private String withdrawalNo;
    private Long providerId;
    private Long bankCardId;
    private Integer amount;
    private String status;          // APPLIED / REJECTED / APPROVED / PAID
    private String rejectReason;
    private LocalDateTime appliedAt;
    private LocalDateTime auditedAt;
    private Long auditorId;
    private LocalDateTime paidAt;
    private String transferNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
