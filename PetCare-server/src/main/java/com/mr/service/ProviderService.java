package com.mr.service;

import com.mr.dto.ProviderLoginDTO;
import com.mr.vo.LoginVO;

public interface ProviderService {

    LoginVO login(ProviderLoginDTO dto);
}