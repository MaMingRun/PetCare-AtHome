package com.mr.mapper;

import com.mr.vo.PriceRuleVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ServicePriceRuleMapper {

    List<PriceRuleVO> listByServiceId(Long serviceId);

    List<com.mr.entity.ServicePriceRule> listEntityByServiceId(Long serviceId);
}
