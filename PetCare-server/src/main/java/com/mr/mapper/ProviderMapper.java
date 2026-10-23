package com.mr.mapper;

import com.mr.entity.Provider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProviderMapper {

    Provider getByOpenid(String openid);

    void insert(Provider provider);

    void updateLastLoginAt(Long id);

    Provider getById(Long id);

    void incrementRejectCount(Long id);

    /** 用户评价后更新服务师四项评分均值 + service_count */
    int updateRating(@Param("id") Long id,
                     @Param("ratingAvg") BigDecimal ratingAvg,
                     @Param("ratingProf") BigDecimal ratingProf,
                     @Param("ratingPunct") BigDecimal ratingPunct,
                     @Param("ratingComm") BigDecimal ratingComm,
                     @Param("ratingCare") BigDecimal ratingCare,
                     @Param("serviceCount") int serviceCount);

    long countTotalActive();

    /** 管理端：列出全部活跃服务师（下拉选择派单用） */
    List<Provider> listActive();

    /** 管理端：服务师分页列表（支持状态/关键字过滤） */
    List<Provider> listPage(@Param("status") String status,
                            @Param("keyword") String keyword,
                            @Param("offset") int offset,
                            @Param("size") int size);

    long countPage(@Param("status") String status,
                   @Param("keyword") String keyword);

    /** 管理端：修改服务师状态（ACTIVE / SUSPENDED / BANNED / QUIT） */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}