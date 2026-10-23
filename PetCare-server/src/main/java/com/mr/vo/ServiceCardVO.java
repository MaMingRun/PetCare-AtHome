package com.mr.vo;

import lombok.Data;

/**
 * 服务卡片VO（小程序服务列表页展示）
 */
@Data
public class ServiceCardVO {

    /** 服务项目主键ID */
    private Long id;

    /** 服务名称，例：上门喂猫、上门遛狗 */
    private String name;

    /** 服务分类，例：喂养服务 / 洗护服务 */
    private String category;

    /** 服务封面图片地址 */
    private String coverUrl;

    /** 起步价，单位：分，前端展示时除以100转为元 */
    private Integer priceFrom;

    /** 单次服务时长，单位：分钟 */
    private Integer durationMinutes;

    /** 标签列表，JSON字符串格式，例：["低应激","实名认证宠托师"]，前端自行解析渲染标签 */
    private String tags;
}