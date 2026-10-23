package com.mr.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务师
 */
@Data
public class Provider {

    private Long id;                     // 主键ID，服务师唯一编号
    private String openid;               // 微信openid：服务师端小程序的微信唯一标识（和用户端openid相互独立）
    private String phone;                // 服务师手机号
    private String realName;             // 真实姓名
    private String idCardNoCipher;       // 身份证号密文：数据库不存明文，加密存储，保护隐私
    private String avatar;               // 服务师头像图片地址
    private String city;                 // 所在城市
    private String serviceAreas;         // JSON字符串：可接单服务区域数组（例如["茂南区","电白区"]）
    private String serviceTypes;         // JSON字符串：可提供的宠物服务类型（上门喂养、遛狗、洗护等）
    private String experiences;          // 从业经历：文字描述，比如“3年猫咪喂养经验”
    private String specialties;          // JSON字符串：专长标签，例如["幼猫护理","大型犬遛护"]
    private String intro;                // 个人简介，自我介绍
    private Integer years;               // 从业年限
    private String status;               // 服务师状态：PENDING待审核 / ACTIVE正常接单 / SUSPENDED暂停接单 / BANNED封禁 / QUIT已离职
    private String level;                // 服务师等级（V1.1版本启用，例如金牌、银牌、普通）
    private BigDecimal ratingAvg;        // 综合平均分
    private BigDecimal ratingProfessional;// 专业评分
    private BigDecimal ratingPunctual;   // 准时评分
    private BigDecimal ratingCommunication;// 沟通评分
    private BigDecimal ratingCare;       // 宠物照料评分
    private Integer serviceCount;        // 已完成服务订单总数
    private Integer rejectCount7d;       // 近7天拒单次数，风控指标，拒单多可限制接单
    private LocalDateTime createdAt;     // 记录创建时间（入驻申请时间）
    private LocalDateTime updatedAt;     // 记录更新时间
    private LocalDateTime deletedAt;     // 逻辑删除时间，不为空代表软删除
}