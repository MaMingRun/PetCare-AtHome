package com.mr.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务师端待响应派单列表项
 */
@Data
public class ProviderDispatchVO {

    private Long dispatchId;        // 派单记录ID
    private Long orderId;           // 订单ID（接单/拒单入参）
    private String orderNo;         // 展示单号
    private String serviceName;     // 服务名称
    private LocalDate startDate;    // 服务开始日期
    private Integer days;           // 预约天数
    private String timeSlot;        // 服务时段

    private String city;            // 城市
    private String district;        // 区/县
    private String community;       // 小区
    private String detail;          // 门牌号
    private String entryMode;       // 入户方式
    private Integer payAmount;      // 服务师收入参考（实付金额，分）
    private String remark;          // 用户备注

    private LocalDateTime responseDeadline; // 响应截止时间
    private LocalDateTime dispatchAt;       // 派单时间

    // 完整地址（序列化时输出为 address 字段）
    public String getAddress() {
        return city + district + community + detail;
    }
}
