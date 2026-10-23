package com.mr.mapper;
import com.mr.entity.Settlement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SettlementMapper {
    void insert(Settlement s);
    int markSettled(@Param("id") Long id);
    List<Settlement> listPending();
    long countPending();
}
