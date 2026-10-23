package com.mr.service;
import com.mr.entity.ProviderWalletFlow;
import com.mr.vo.WalletVO;
import java.util.List;

public interface WalletQueryService {
    WalletVO getWallet(Long providerId);
    List<ProviderWalletFlow> listFlows(Long providerId);
}
