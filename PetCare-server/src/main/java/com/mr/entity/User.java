package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户（宠物主人）
 */
@Data
public class User {

    private Long id;                // 主键ID，用户唯一编号（数据库自增主键）
    private String openid;          // 微信openid：小程序用户唯一标识，一个微信账号对应一个小程序openid
    private String unionid;         // 微信unionid：同一微信主体下（同一个公众号/小程序/开放平台），同一个微信用户共用的唯一ID，跨小程序互通
    private String nickname;        // 用户微信昵称
    private String avatar;          // 用户微信头像url地址
    private String phone;           // 用户手机号明文，接口返回时需要脱敏展示（如138****1234）
    private Integer complaintMarked;// 投诉标记：标记该用户是否有投诉记录，0无/1有（自定义业务标识）
    private Integer blacklisted;    // 是否黑名单：0=不是黑名单，1=拉入黑名单
    private String blacklistReason; // 黑名单原因：被拉黑时记录备注信息
    private String status;          // 用户状态：枚举 NORMAL正常 / BANNED封禁 / CANCELLED已注销
    private LocalDateTime lastLoginAt;  // 最后登录时间
    private LocalDateTime createdAt;    // 创建时间：用户注册时间
    private LocalDateTime updatedAt;    // 更新时间：记录信息最后修改时间
    private LocalDateTime deletedAt;    // 逻辑删除时间：不为null代表已软删除；null代表正常存在（逻辑删除）
}