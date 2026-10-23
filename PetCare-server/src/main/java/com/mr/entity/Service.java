package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 服务类目
 */
@Data
public class Service {

    private Long id;                // 主键ID，服务类目唯一编号
    private String name;            // 服务名称，例：上门喂养、上门遛狗
    private String category;        // 服务大类：FEEDING喂养 / GROOMING洗护 / MEDICAL医护
    private String coverUrl;        // 服务封面图片地址，小程序首页展示用
    private Integer priceFrom;      // 起步价，单位：分（数据库存分为了避免浮点数精度问题，前端展示转元）
    private Integer durationMinutes;// 单次服务标准时长，单位分钟
    private String tags;            // JSON字符串：服务标签，例如["适合猫咪","新手友好"]
    private String contentItems;    // JSON字符串：服务内容清单，例：["添粮换水","清理猫砂","拍照反馈"]
    private String notice;          // 服务须知：下单前给用户阅读的注意事项
    private String refundPolicy;    // 退款规则文案，发生退款时参考
    private String status;          // 上下架状态：ON上架，OFF下架（下架后小程序不可见，不能下单）
    private Integer sort;           // 排序权重，数字越大，小程序页面展示越靠前
    private LocalDateTime createdAt;// 创建时间
    private LocalDateTime updatedAt;// 更新时间
    private LocalDateTime deletedAt;// 逻辑删除时间，不为null代表软删除
}