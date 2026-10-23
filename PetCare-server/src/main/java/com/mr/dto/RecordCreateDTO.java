package com.mr.dto;

import lombok.Data;

@Data
public class RecordCreateDTO {
    private String type;      // PHOTO / VIDEO / NOTE
    private String mediaUrl;  // PHOTO/VIDEO 必填
    private String remark;    // NOTE 必填，其他可选
}