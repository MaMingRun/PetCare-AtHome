package com.mr.vo;

import lombok.Data;

/**
 * 三端通用登录返回
 */
@Data
public class LoginVO {

    private Long id;
    private String name;   //用户=昵称；服务师/管理员=真实姓名
    private String status; //用户 NORMAL；服务师 PENDING（前端据此引导去入驻）
    private String token;
}