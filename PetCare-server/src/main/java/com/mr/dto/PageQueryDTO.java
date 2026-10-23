package com.mr.dto;
import lombok.Data;

@Data
public class PageQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    /** 状态过滤，可空 */
    private String status;
    /** 业务单号/手机号模糊搜索，可空 */
    private String keyword;
}
