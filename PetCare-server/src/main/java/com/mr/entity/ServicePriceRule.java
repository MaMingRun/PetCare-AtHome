package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 服务计价规则
 */
@Data
public class ServicePriceRule {

    private Long id;                // 主键ID，计价规则唯一编号
    private Long serviceId;        // 关联服务类目ID（Service表id），一个服务可以有多条计价规则
    private String ruleType;       // 规则类型枚举：BASE基础价 / EXTRA_PET额外宠物加价 / WEIGHT体重加价 / DISTANCE距离加价
    private String label;          // 前端展示名称，例：「每加一只猫」「超出3公里加价」
    private Integer amount;        // 加价金额，单位：分
    private String ruleConfig;     // JSON字符串：扩展配置参数，用来定义规则生效条件
    private LocalDateTime createdAt;// 记录创建时间
    private LocalDateTime updatedAt;// 记录更新时间
}
