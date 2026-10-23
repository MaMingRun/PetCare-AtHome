package com.mr.mapper;
import com.mr.entity.Withdrawal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WithdrawalMapper {
    void insert(Withdrawal w);
    Withdrawal getById(@Param("id") Long id);
    int markApproved(@Param("id") Long id, @Param("auditorId") Long auditorId);
    int markRejected(@Param("id") Long id, @Param("auditorId") Long auditorId, @Param("reason") String reason);
    int markPaid(@Param("id") Long id, @Param("transferNo") String transferNo);

    // --- 管理端分页列表 ---
    List<Withdrawal> listPage(@Param("status") String status,
                              @Param("offset") int offset,
                              @Param("size") int size);
    long countPage(@Param("status") String status);
}
