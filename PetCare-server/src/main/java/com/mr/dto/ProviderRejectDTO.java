package com.mr.dto;

import lombok.Data;

/**
 * 服务师拒单请求
 */
@Data
public class ProviderRejectDTO {

    private String remark;  // 拒单原因
}
