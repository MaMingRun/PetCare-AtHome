package com.mr.service;

import com.mr.dto.UserLoginDTO;
import com.mr.vo.LoginVO;

public interface UserService {

    LoginVO login(UserLoginDTO dto);

    void bindPhone(Long userId, String phone);
}