package com.mr.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Integer scoreProfessional;
    private Integer scorePunctual;
    private Integer scoreCommunication;
    private Integer scoreCare;
    private String content;
    private String images;
    private Integer anonymous;
    private LocalDateTime createdAt;
}
