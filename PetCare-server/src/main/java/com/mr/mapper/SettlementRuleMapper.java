package com.mr.mapper;
import com.mr.entity.SettlementRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SettlementRuleMapper {
    SettlementRule getByCity(@Param("city") String city);
}
