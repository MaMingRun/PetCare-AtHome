package com.mr.controller.admin;

import com.mr.common.result.Result;
import com.mr.dto.AdminLoginDTO;
import com.mr.service.AdminService;
import com.mr.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody AdminLoginDTO dto) {
        return Result.success(adminService.login(dto));
    }
}