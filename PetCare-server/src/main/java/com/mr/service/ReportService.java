package com.mr.service;

import com.mr.dto.ReportCreateDTO;
import com.mr.vo.ReportVO;

public interface ReportService {

    /** 提交每日服务报告（服务师端） */
    ReportVO submit(Long orderId, Long providerId, ReportCreateDTO dto);
}