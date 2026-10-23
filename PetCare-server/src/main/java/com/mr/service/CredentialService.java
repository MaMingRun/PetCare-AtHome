package com.mr.service;

import com.mr.vo.CredentialLogVO;
import com.mr.vo.CredentialVO;
import java.util.List;

public interface CredentialService {

    /** 服务师查看凭证（仅 ARRIVED/SERVING 状态的本人单允许） */
    CredentialVO view(Long orderId, Long providerId, String ip);

    /** 服务师使用凭证（模拟开锁，仅写日志） */
    void unlock(Long orderId, Long providerId, String ip);

    /** 用户端透明展示：查看某订单的全部凭证操作 */
    List<CredentialLogVO> listByOrder(Long orderId, Long userId);
}
