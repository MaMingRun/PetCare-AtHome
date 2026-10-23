package com.mr.mapper;
import com.mr.entity.ProviderWalletFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProviderWalletFlowMapper {
    void insert(ProviderWalletFlow flow);
    List<ProviderWalletFlow> listByProvider(@Param("providerId") Long providerId);

    /** 管理端：全平台钱包流水分页（可按服务师/类型筛选） */
    List<ProviderWalletFlow> listPage(@Param("providerId") Long providerId,
                                      @Param("bizType") String bizType,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    long countPage(@Param("providerId") Long providerId,
                   @Param("bizType") String bizType);
}
