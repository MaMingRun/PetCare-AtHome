package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.ProviderWallet;
import com.mr.entity.ProviderWalletFlow;
import com.mr.entity.Settlement;
import com.mr.mapper.ProviderWalletFlowMapper;
import com.mr.mapper.ProviderWalletMapper;
import com.mr.mapper.SettlementMapper;
import com.mr.service.SettlementSettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettlementSettleServiceImpl implements SettlementSettleService {

    @Autowired private SettlementMapper settlementMapper;
    @Autowired private ProviderWalletMapper walletMapper;
    @Autowired private ProviderWalletFlowMapper flowMapper;

    @Override
    @Transactional
    public int settleAll() {
        List<Settlement> pending = settlementMapper.listPending();
        int ok = 0;
        for (Settlement s : pending) {
            ProviderWallet w = walletMapper.getById(s.getProviderId());
            if (w == null) continue;
            int rows = walletMapper.settlePending(s.getProviderId(), s.getNetAmount(), w.getVersion());
            if (rows == 0) throw new BaseException("钱包版本冲突，请重试");

            int sr = settlementMapper.markSettled(s.getId());
            if (sr == 0) throw new BaseException("结算单已处理");

            ProviderWalletFlow flow = new ProviderWalletFlow();
            flow.setProviderId(s.getProviderId());
            flow.setBizType("INCOME_SETTLED");
            flow.setAmount(s.getNetAmount());
            flow.setBalanceAfter(w.getBalance() + s.getNetAmount());
            flow.setBizId(s.getId());
            flow.setRemark("订单 #" + s.getOrderId() + " 结算到余额");
            flowMapper.insert(flow);
            ok++;
        }
        return ok;
    }
}
