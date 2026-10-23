package com.mr.mapper;
import com.mr.entity.ProviderBankCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProviderBankCardMapper {
    ProviderBankCard getById(@Param("id") Long id);
}
