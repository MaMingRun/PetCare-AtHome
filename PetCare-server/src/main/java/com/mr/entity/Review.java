package com.mr.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long orderId;
    private Long userId;
    private Long providerId;
    private Integer scoreProfessional;
    private Integer scorePunctual;
    private Integer scoreCommunication;
    private Integer scoreCare;
    private String content;
    private String images;          // JSON 字符串
    private Integer anonymous;      // 0/1
    private LocalDateTime createdAt;
}
