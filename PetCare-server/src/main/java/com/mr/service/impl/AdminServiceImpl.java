package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.common.properties.JwtProperties;
import com.mr.common.utils.JwtUtil;
import com.mr.dto.AdminLoginDTO;
import com.mr.entity.AdminUser;
import com.mr.mapper.AdminUserMapper;
import com.mr.service.AdminService;
import com.mr.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public LoginVO login(AdminLoginDTO dto) {
        AdminUser admin = adminUserMapper.getByUsername(dto.getUsername());
        //统一报错：不区分"用户不存在"和"密码错误"，防账号探测
        if (admin == null || !encoder.matches(dto.getPassword(), admin.getPasswordHash())) {
            throw new BaseException("用户名或密码错误");
        }
        if (!"ACTIVE".equals(admin.getStatus())) {
            throw new BaseException("账号已禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("empId", admin.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(), claims);

        LoginVO vo = new LoginVO();
        vo.setId(admin.getId());
        vo.setName(admin.getRealName());
        vo.setStatus(admin.getStatus());
        vo.setToken(token);
        return vo;
    }
}