package com.mr.vo;

import lombok.Data;
import java.util.List;

/**
 * 服务详情VO（小程序服务详情页）
 */
@Data
public class ServiceDetailVO {

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

    /** 服务包含项目，JSON字符串，例：["添粮换水","清理猫砂","陪玩5分钟"]，前端解析展示服务内容清单 */
    private String contentItems;

    /** 服务须知，文字描述，下单前用户阅读提醒 */
    private String notice;

    /** 退款政策说明，文字描述 */
    private String refundPolicy;

    /** 计价规则列表，包含基础价、额外宠物加价、距离加价等明细 */
    private List<PriceRuleVO> priceRules;
}