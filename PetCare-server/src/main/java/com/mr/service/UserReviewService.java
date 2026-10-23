package com.mr.service;
import com.mr.dto.ReviewCreateDTO;
import com.mr.vo.ReviewVO;

public interface UserReviewService {
    ReviewVO submit(Long orderId, Long userId, ReviewCreateDTO dto);
}
