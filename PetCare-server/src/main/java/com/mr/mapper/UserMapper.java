package com.mr.mapper;
import com.mr.entity.User;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User getByOpenid(String openid);

    User getById(Long id);

    void insert(User user);

    void updatePhone(Long id, String phone);

    void updateLastLoginAt(Long id);

    /** 管理端：用户分页 */
    List<User> listPage(@Param("keyword") String keyword,
                        @Param("status") String status,
                        @Param("offset") int offset,
                        @Param("size") int size);

    long countPage(@Param("keyword") String keyword,
                   @Param("status") String status);

    /** 管理端：拉入/移出黑名单 */
    int updateBlacklist(@Param("id") Long id,
                        @Param("blacklisted") Integer blacklisted,
                        @Param("reason") String reason);
}
