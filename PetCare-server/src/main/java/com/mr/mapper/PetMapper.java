package com.mr.mapper;

import com.mr.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PetMapper {

    /** 按 id 批量查当前用户的宠物（归属校验在 SQL 内完成） */
    List<Pet> listByIdsAndUser(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /** 我的宠物列表 */
    List<Pet> listByUser(@Param("userId") Long userId);

    /** 新增（user_id 由 Controller 注入） */
    void insert(Pet pet);

    /** 修改（归属在 SQL 内校验，影响 0 行=越权或不存在） */
    int updateByUser(Pet pet);

    /** 删除（软删，归属在 SQL 内校验） */
    int deleteByUser(@Param("id") Long id, @Param("userId") Long userId);
}
