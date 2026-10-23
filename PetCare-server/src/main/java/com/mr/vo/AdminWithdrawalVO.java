package com.mr.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminWithdrawalVO {
    private Long id;
    private String withdrawalNo;
    private Long providerId;
    private String providerName;
    private String bankCardMasked;
    private Integer amount;
    private String status;
    private String rejectReason;
    private LocalDateTime appliedAt;
    private LocalDateTime paidAt;
}
