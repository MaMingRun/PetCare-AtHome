package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderAlert {
    private Long id;
    private Long orderId;
    private String alertType;     // DEVIATION / REPORT_ABNORMAL / NO_REPORT
    private String level;         // LOW / MEDIUM / HIGH，默认 LOW
    private String alertText;
    private Integer handled;      // 0=未处理 1=已处理
    private Long handlerId;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
