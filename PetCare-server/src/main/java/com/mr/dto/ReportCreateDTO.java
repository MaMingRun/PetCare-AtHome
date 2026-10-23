package com.mr.dto;

import lombok.Data;

@Data
public class ReportCreateDTO {
    private String eating;
    private String drinking;
    private String excretion;
    private String spirit;
    /** 异常列表，JSON 字符串，可空 */
    private String abnormalities;
    /** 照片 URL 数组，JSON 字符串，可空 */
    private String photos;
    private String summary;
}