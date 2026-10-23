package com.mr.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String code;  //wx.login 获取的临时凭证（dev 下为任意自定义标识）
}