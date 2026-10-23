package com.mr.config;

import com.mr.entity.AdminUser;
import com.mr.mapper.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * dev 环境种子数据：admin_user 无账号时自动创建 admin/123456
 */
@Component
@Profile("dev")
@Slf4j
public class DevSeedRunner implements CommandLineRunner {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public void run(String... args) {
        if (adminUserMapper.getByUsername("admin") != null) {
            return;
        }
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPasswordHash(new BCryptPasswordEncoder().encode("123456"));
        admin.setRealName("超级管理员");
        admin.setRoleId(1L);
        admin.setStatus("ACTIVE");
        adminUserMapper.insert(admin);
        log.info("[种子数据] 已创建管理员 admin/123456");
    }
}
