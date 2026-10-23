package com.mr.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务过程记录（打卡/照片/视频/文字）
 */
@Data
public class ServiceRecord {

    private Long id;
    private Long orderId;        // 主订单ID
    private Long dayId;          // 归属的 order_day.id（记录按天归组）
    private Long providerId;     // 留痕的服务师
    private String type;         // CHECKIN打卡 / PHOTO照片 / VIDEO视频 / NOTE文字
    private String sopItemCode;  // 对应的 SOP 项编码（后续步骤用）
    private String mediaUrl;     // 媒体文件地址（PHOTO/VIDEO 必填）
    private Integer watermarked; // 是否已合成时间+定位水印：1是 0否（客户端职责）
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer distanceMeters; // 打卡点与订单地址的偏差距离（米），仅 CHECKIN
    private Integer deviationAlert; // 偏差>500m 触发告警：1是 0否
    private String remark;
    private LocalDateTime capturedAt; // 拍摄/打卡发生时间
    private LocalDateTime createdAt;
}
