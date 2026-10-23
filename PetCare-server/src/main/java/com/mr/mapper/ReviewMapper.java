package com.mr.mapper;
import com.mr.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper {
    void insert(Review r);
    Review getByOrderId(@Param("orderId") Long orderId);

    /** 管理端：评价分页（可按服务师/评分筛选） */
    List<Review> listPage(@Param("providerId") Long providerId,
                          @Param("minScore") Integer minScore,
                          @Param("offset") int offset,
                          @Param("size") int size);

    long countPage(@Param("providerId") Long providerId,
                   @Param("minScore") Integer minScore);

    /** 管理端：评价统计（总数、各维度均分、分布） */
    Map<String, Object> statSummary(@Param("providerId") Long providerId);
}
