package com.mr.mapper;

import com.mr.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper {

    AdminUser getByUsername(String username);

    void insert(AdminUser admin);
}