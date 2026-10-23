package com.mr.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CredentialLogVO {
    private Long id;
    private String action;           // VIEW / UNLOCK
    private Long providerId;
    private String ip;
    private LocalDateTime createdAt;
}