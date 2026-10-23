package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.common.properties.JwtProperties;
import com.mr.common.utils.JwtUtil;
import com.mr.dto.UserLoginDTO;
import com.mr.entity.User;
import com.mr.gateway.WechatGateway;
import com.mr.mapper.UserMapper;
import com.mr.service.UserService;
import com.mr.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WechatGateway wechatGateway;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public LoginVO login(UserLoginDTO dto) {
        //1. code 换 openid（dev 为 Mock）
        String openid = wechatGateway.getOpenid(dto.getCode());

        //2. 查用户，不存在则注册
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("宠物家长" + (int) (Math.random() * 10000));
            userMapper.insert(user); //自增 id 回填到 user.id
        }

        //3. 更新登录时间
        userMapper.updateLastLoginAt(user.getId());

        //4. 签发 JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(), claims);

        //5. 组装返回
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setName(user.getNickname());
        vo.setStatus(user.getStatus());
        vo.setToken(token);
        return vo;
    }

    @Override
    public void bindPhone(Long userId, String phone) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BaseException("手机号格式不正确");
        }
        userMapper.updatePhone(userId, phone);
    }
}
