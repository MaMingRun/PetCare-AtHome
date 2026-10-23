package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.Service;
import com.mr.mapper.ServiceMapper;
import com.mr.mapper.ServicePriceRuleMapper;
import com.mr.service.ShopService;
import com.mr.vo.PriceRuleVO;
import com.mr.vo.ServiceCardVO;
import com.mr.vo.ServiceDetailVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@org.springframework.stereotype.Service   //全限定名，避让实体类 Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ServiceMapper serviceMapper;
    @Autowired
    private ServicePriceRuleMapper priceRuleMapper;

    @Override
    public List<ServiceCardVO> listServices(String category) {
        return serviceMapper.listOnShelf(category);
    }

    @Override
    public ServiceDetailVO getServiceDetail(Long id) {
        Service service = serviceMapper.getOnShelfById(id);
        if (service == null) {
            throw new BaseException("服务不存在或已下架");
        }
        ServiceDetailVO vo = new ServiceDetailVO();
        vo.setId(service.getId());
        vo.setName(service.getName());
        vo.setCategory(service.getCategory());
        vo.setCoverUrl(service.getCoverUrl());
        vo.setPriceFrom(service.getPriceFrom());
        vo.setDurationMinutes(service.getDurationMinutes());
        vo.setTags(service.getTags());
        vo.setContentItems(service.getContentItems());
        vo.setNotice(service.getNotice());
        vo.setRefundPolicy(service.getRefundPolicy());
        vo.setPriceRules(priceRuleMapper.listByServiceId(id));
        return vo;
    }
}