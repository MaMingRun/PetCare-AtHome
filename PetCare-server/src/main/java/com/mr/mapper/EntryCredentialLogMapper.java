package com.mr.mapper;

import com.mr.entity.EntryCredentialLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EntryCredentialLogMapper {

    void insert(EntryCredentialLog log);

    /** 某订单的全部凭证操作日志（按时间升序） */
    List<EntryCredentialLog> listByOrder(@Param("orderId") Long orderId);
}
