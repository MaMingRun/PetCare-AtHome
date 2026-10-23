package com.mr.mapper;

import com.mr.entity.Service;
import com.mr.vo.ServiceCardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceMapper {

    /** 上架中的服务卡片列表，category 可为 null（查全部） */
    List<ServiceCardVO> listOnShelf(String category);

    /** 详情：仅查上架中的，按 id */
    Service getOnShelfById(Long id);

    /** 管理端：服务分页（含上下架状态过滤） */
    List<Service> listPage(@Param("status") String status,
                           @Param("category") String category,
                           @Param("keyword") String keyword,
                           @Param("offset") int offset,
                           @Param("size") int size);

    long countPage(@Param("status") String status,
                   @Param("category") String category,
                   @Param("keyword") String keyword);

    Service getById(@Param("id") Long id);

    /** 管理端：新增服务 */
    int insertService(Service service);

    /** 管理端：编辑服务（按 id 全字段更新） */
    int updateService(Service service);

    /** 管理端：上下架切换 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
