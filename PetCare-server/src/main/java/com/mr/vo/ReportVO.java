package com.mr.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private String eating;
    private String drinking;
    private String excretion;
    private String spirit;
    private String abnormalities;
    private String photos;
    private String summary;
    private LocalDateTime submittedAt;
}
