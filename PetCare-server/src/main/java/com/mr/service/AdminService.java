package com.mr.service;

import com.mr.dto.AdminLoginDTO;
import com.mr.vo.LoginVO;

public interface AdminService {

    LoginVO login(AdminLoginDTO dto);
}
