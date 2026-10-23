package com.mr.service.impl;

import com.mr.common.properties.JwtProperties;
import com.mr.common.utils.JwtUtil;
import com.mr.dto.ProviderLoginDTO;
import com.mr.entity.Provider;
import com.mr.gateway.WechatGateway;
import com.mr.mapper.ProviderMapper;
import com.mr.service.ProviderService;
import com.mr.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProviderServiceImpl implements ProviderService {

    @Autowired
    private ProviderMapper providerMapper;
    @Autowired
    private WechatGateway wechatGateway;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public LoginVO login(ProviderLoginDTO dto) {
        String openid = wechatGateway.getOpenid(dto.getCode());

        Provider provider = providerMapper.getByOpenid(openid);
        if (provider == null) {
            provider = new Provider();
            provider.setOpenid(openid);
            providerMapper.insert(provider); //建壳账号，状态 PENDING
        }

        providerMapper.updateLastLoginAt(provider.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("providerId", provider.getId());
        String token = JwtUtil.createJWT(jwtProperties.getProviderSecretKey(),
                jwtProperties.getProviderTtl(), claims);

        LoginVO vo = new LoginVO();
        vo.setId(provider.getId());
        vo.setName(provider.getRealName());
        vo.setStatus(provider.getStatus());
        vo.setToken(token);
        return vo;
    }
}
