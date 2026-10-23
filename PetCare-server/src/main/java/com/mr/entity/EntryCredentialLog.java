package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EntryCredentialLog {
    private Long id;
    private Long addressId;
    private Long orderId;
    private Long providerId;
    private String action;       // VIEW / UNLOCK
    private String ip;
    private LocalDateTime createdAt;
}
