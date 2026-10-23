package com.mr.controller.provider;

import com.mr.common.result.Result;
import com.mr.dto.ProviderLoginDTO;
import com.mr.service.ProviderService;
import com.mr.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider/auth")
public class ProviderAuthController {

    @Autowired
    private ProviderService providerService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody ProviderLoginDTO dto) {
        return Result.success(providerService.login(dto));
    }
}