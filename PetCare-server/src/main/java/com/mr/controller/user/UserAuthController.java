package com.mr.controller.user;

import com.mr.common.context.BaseContext;
import com.mr.common.result.Result;
import com.mr.dto.BindPhoneDTO;
import com.mr.dto.UserLoginDTO;
import com.mr.service.UserService;
import com.mr.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/auth")
public class UserAuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录（登录接口，拦截器已放行）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 绑定手机号（dev 手动输入；需要携带 token）
     */
    @PostMapping("/bind-phone")
    public Result<String> bindPhone(@RequestBody BindPhoneDTO dto) {
        userService.bindPhone(BaseContext.getCurrentId(), dto.getPhone());
        return Result.success();
    }
}
