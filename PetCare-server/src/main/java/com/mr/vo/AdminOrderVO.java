package com.mr.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端派单池订单列表项
 */
@Data
public class AdminOrderVO {

    private Long id;               // 订单ID
    private String orderNo;        // 展示单号
    private String serviceName;    // 服务名称快照
    private String status;         // 订单状态
    private LocalDate startDate;   // 服务开始日期
    private Integer days;          // 预约天数
    private String timeSlot;       // 服务时段

    private String contactName;    // 联系人
    private String contactPhone;   // 联系电话
    private String address;        // 完整地址（市/区/小区/门牌拼接）
    private String entryMode;      // 入户方式
    private Integer payAmount;     // 实付金额（分）
    private String remark;         // 用户备注

    private List<String> petNames; // 订单宠物名字快照
    private LocalDateTime createdAt; // 下单时间
}
