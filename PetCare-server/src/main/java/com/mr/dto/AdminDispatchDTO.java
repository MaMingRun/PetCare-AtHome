package com.mr.dto;

import lombok.Data;

/**
 * 管理端派单请求
 */
@Data
public class AdminDispatchDTO {

    private Long providerId;  // 指派的服务师ID
    private String remark;    // 派单备注（可空）
}
