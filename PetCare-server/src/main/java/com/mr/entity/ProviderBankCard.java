package com.mr.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProviderBankCard {
    private Long id;
    private Long providerId;
    private String holderName;
    private String bankName;
    private String cardNoCipher;
    private String cardNoMasked;
    private String phone;
    private Integer isDefault;
    private LocalDateTime deletedAt;
}
