package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.ProviderWallet;
import com.mr.entity.ProviderWalletFlow;
import com.mr.mapper.ProviderWalletFlowMapper;
import com.mr.mapper.ProviderWalletMapper;
import com.mr.service.WalletQueryService;
import com.mr.vo.WalletVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletQueryServiceImpl implements WalletQueryService {

    @Autowired private ProviderWalletMapper walletMapper;
    @Autowired private ProviderWalletFlowMapper flowMapper;

    @Override
    public WalletVO getWallet(Long providerId) {
        ProviderWallet w = walletMapper.getById(providerId);
        if (w == null) throw new BaseException("钱包不存在");
        WalletVO vo = new WalletVO();
        vo.setProviderId(providerId);
        vo.setBalance(w.getBalance());
        vo.setPendingSettlement(w.getPendingSettlement());
        vo.setTotalIncome(w.getTotalIncome());
        return vo;
    }

    @Override
    public List<ProviderWalletFlow> listFlows(Long providerId) {
        return flowMapper.listByProvider(providerId);
    }
}
