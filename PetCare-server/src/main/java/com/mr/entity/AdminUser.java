package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理后台用户
 */
@Data
public class AdminUser {

    private Long id;                // 主键ID，管理员唯一编号
    private String username;        // 管理员登录账号（后台系统登录名）
    private String passwordHash;    // bcrypt加密后的密码，数据库不存明文密码
    private String realName;        // 管理员真实姓名
    private Long roleId;            // 角色ID，关联角色表，用来控制权限（如超级管理员、运营、客服）
    private String status;          // 账号状态：ACTIVE启用 / DISABLED禁用
    private LocalDateTime lastLoginAt;  // 最后登录时间
    private LocalDateTime createdAt;    // 账号创建时间
    private LocalDateTime updatedAt;    // 账号信息最后更新时间
    private LocalDateTime deletedAt;    // 逻辑删除时间；不为null代表软删除
}