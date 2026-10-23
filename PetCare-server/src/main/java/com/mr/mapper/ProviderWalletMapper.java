package com.mr.mapper;
import com.mr.entity.ProviderWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProviderWalletMapper {
    ProviderWallet getById(@Param("providerId") Long providerId);
    int addPending(@Param("providerId") Long providerId,
                   @Param("netAmount") int netAmount,
                   @Param("version") int version);
    int settlePending(@Param("providerId") Long providerId,
                      @Param("netAmount") int netAmount,
                      @Param("version") int version);
    int deductBalance(@Param("providerId") Long providerId,
                      @Param("amount") int amount,
                      @Param("version") int version);
    int refundBalance(@Param("providerId") Long providerId,
                      @Param("amount") int amount,
                      @Param("version") int version);
}
