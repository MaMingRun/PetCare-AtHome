package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceReport {
    private Long id;
    private Long orderId;
    private Long dayId;
    private Long providerId;
    private String eating;           // 正常/偏少/拒食/呕吐
    private String drinking;          // 正常/偏少/过量
    private String excretion;        // 正常/便秘/腹泻/带血
    private String spirit;            // 活泼/正常/萎靡
    private String abnormalities;    // JSON 字符串，异常列表（空串或 null 代表无异常）
    private String photos;            // JSON 数组字符串，照片 URL
    private String summary;           // 自由文字
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
