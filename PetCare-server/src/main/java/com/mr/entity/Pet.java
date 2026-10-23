package com.mr.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物档案
 */
@Data
public class Pet {

    private Long id;                // 主键ID，宠物档案唯一编号
    private Long userId;            // 所属用户ID，关联User表（宠物主人）
    private String name;            // 宠物名字
    private String species;         // 物种：CAT猫 / DOG狗 / OTHER其他宠物
    private String breed;           // 品种，例如：布偶猫、金毛
    private String gender;          // 性别：MALE公 / FEMALE母 / UNKNOWN未知
    private LocalDate birthday;     // 宠物生日
    private BigDecimal weightKg;    // 体重，单位kg
    private Integer sterilized;     // 是否绝育：0未绝育，1已绝育
    private String avatar;          // 宠物头像图片链接
    private String personalityTags; // JSON数组：性格标签，如["胆小","粘人","活泼"]
    private String personalityNote; // 性格备注，文字描述宠物习性
    private String allergies;       // 过敏与禁忌：食物、药物过敏等信息
    private String chronicDisease;  // 慢病与用药：基础病、需要定时喂药说明
    private String feedingInstruction; // JSON数组：喂养指令，["早晚各喂一次","不能喂牛奶"]
    private LocalDateTime createdAt; // 档案创建时间
    private LocalDateTime updatedAt; // 档案更新时间
    private LocalDateTime deletedAt; // 逻辑删除时间，不为null代表软删除
}