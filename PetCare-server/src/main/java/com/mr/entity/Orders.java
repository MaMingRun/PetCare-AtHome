package com.mr.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
public class Orders {

    private Long id;                     // 订单主键ID
    private String orderNo;              // 展示单号，对外给用户看的订单编号（业务流水号）
    private Long userId;                 // 用户id，关联User表（下单的宠物主人）
    private Long serviceId;              // 服务类目id，关联Service表
    private String serviceName;          // 下单时服务名称快照（防止后续服务类目改名，订单历史错乱）
    private Long providerId;             // 服务师ID，**接单之后才回填**，未接单时为null
    private String status;               // 订单状态，取自OrderStatus枚举名称，如待支付、待接单、服务中、已完成、已取消
    private LocalDate startDate;         // 服务开始日期
    private Integer days;                // 连续预约的天数（比如连续3天上门喂养）
    private String timeSlot;             // 服务时间段，示例：14:00-15:00
    private String remark;               // 用户下单备注信息

    // --- 地址快照（下单时保存，防止用户后续修改地址，历史订单地址不变） ---
    private String contactName;          // 联系人姓名
    private String contactPhone;          // 联系人手机号
    private String city;                 // 城市
    private String district;             // 区/县
    private String community;            // 小区/社区
    private String detail;               // 详细门牌号
    private BigDecimal longitude;        // 经度
    private BigDecimal latitude;          // 纬度
    private String entryMode;            // 入户方式枚举：SMART_LOCK智能锁 / PROPERTY物业 / KEY_DEPOSIT钥匙寄存 / AT_HOME主人在家
    private Long addressId;

    // --- 金额，单位全部【分】，避免浮点精度问题 ---
    private Integer baseAmount;          // 基础服务费用
    private Integer extraAmount;          // 附加费用（如宠物多只加价、额外清理等）
    private Integer discountAmount;      // 优惠减免金额
    private Integer totalAmount;         // 订单总金额（优惠前）
    private Integer payAmount;            // 用户实际需要支付金额（优惠后）

    // --- 订单各个时间节点 ---
    private LocalDateTime payExpireAt;    // 支付超时截止时间，超时未支付自动取消订单
    private LocalDateTime payTime;        // 用户实际付款时间
    private LocalDateTime dispatchDeadline;// 派单截止时间，到点还没服务师接单自动取消
    private LocalDateTime confirmDeadline; // 服务师确认接单截止时间
    private LocalDateTime confirmedAt;    // 服务师确认接单时间
    private LocalDateTime reviewDeadline; // 服务完成后评价截止时间，超时自动好评
    private LocalDateTime cancelTime;     // 订单取消时间
    private String cancelReasonCode;      // 取消原因编码（用于统计，如用户取消/超时未支付/无服务师接单）
    private String cancelRemark;          // 取消备注文字说明
    private Integer penaltyAmount;        // 违约金（比如用户临时取消扣的钱，单位分）
    private Integer refundAmount;         // 退款金额（单位分）

    // --- 审计字段 ---
    private LocalDateTime createdAt;      // 订单创建时间
    private LocalDateTime updatedAt;      // 订单信息最后更新时间
    private LocalDateTime deletedAt;      // 逻辑删除时间，不为null代表软删除
}
