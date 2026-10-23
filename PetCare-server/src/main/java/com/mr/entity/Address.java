package com.mr.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户地址与入户方式
 */
@Data
public class Address {

    private Long id;                     // 主键ID，地址记录唯一编号
    private Long userId;                 // 用户ID，关联User表，一个用户可保存多条地址（一对多）
    private String contactName;          // 联系人姓名
    private String contactPhone;         // 联系人手机号
    private String city;                 // 城市
    private String district;             // 区/县
    private String community;            // 小区名称
    private String detail;               // 详细地址（楼栋、单元、门牌号）
    private BigDecimal longitude;        // 经度，用于计算距离、派单
    private BigDecimal latitude;         // 纬度
    private String entryMode;            // 入户方式枚举：SMART_LOCK智能锁 / PROPERTY物业开门 / KEY_DEPOSIT钥匙寄存 / AT_HOME主人在家
    private String entrySecretCipher;    // 入户凭证密文，智能锁密码等敏感信息加密存储，不存明文
    private Integer isDefault;           // 是否默认地址：1=默认，0=普通地址；下单页面自动优先选中
    private LocalDateTime createdAt;     // 地址创建时间
    private LocalDateTime updatedAt;     // 地址信息更新时间
    private LocalDateTime deletedAt;     // 逻辑删除时间；不为null代表软删除
}
