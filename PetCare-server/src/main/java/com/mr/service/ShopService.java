package com.mr.service;

import com.mr.vo.ServiceCardVO;
import com.mr.vo.ServiceDetailVO;
import java.util.List;

public interface ShopService {

    /** 服务列表，category 为 null 时查全部分类 */
    List<ServiceCardVO> listServices(String category);

    /** 服务详情（含计价规则） */
    ServiceDetailVO getServiceDetail(Long id);
}