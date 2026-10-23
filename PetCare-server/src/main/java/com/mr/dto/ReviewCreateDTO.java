package com.mr.dto;
import lombok.Data;

@Data
public class ReviewCreateDTO {
    private Integer scoreProfessional;
    private Integer scorePunctual;
    private Integer scoreCommunication;
    private Integer scoreCare;
    private String content;
    private String images;
    private Integer anonymous;
}
