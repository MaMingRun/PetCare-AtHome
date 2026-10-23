package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.*;
import com.mr.mapper.*;
import com.mr.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Autowired private WithdrawalMapper withdrawalMapper;
    @Autowired private ProviderWalletMapper walletMapper;
    @Autowired private ProviderWalletFlowMapper flowMapper;
    @Autowired private SettlementRuleMapper ruleMapper;
    @Autowired private ProviderBankCardMapper bankCardMapper;

    @Override
    @Transactional
    public Long apply(Long providerId, Integer amount, Long bankCardId) {
        if (amount == null || amount <= 0) throw new BaseException("提现金额必须大于 0");

        SettlementRule rule = ruleMapper.getByCity("");
        if (rule == null) throw new BaseException("未找到结算规则");
        if (amount < rule.getMinWithdrawAmount()) {
            throw new BaseException("最低提现金额 " + rule.getMinWithdrawAmount() / 100 + " 元");
        }

        ProviderBankCard card = bankCardMapper.getById(bankCardId);
        if (card == null || !providerId.equals(card.getProviderId())) {
            throw new BaseException("银行卡不存在");
        }

        ProviderWallet w = walletMapper.getById(providerId);
        if (w == null) throw new BaseException("钱包不存在");
        if (w.getBalance() < amount) throw new BaseException("余额不足");

        int rows = walletMapper.deductBalance(providerId, amount, w.getVersion());
        if (rows == 0) throw new BaseException("钱包冲突，请重试");

        Withdrawal wd = new Withdrawal();
        wd.setWithdrawalNo("WD" + System.currentTimeMillis());
        wd.setProviderId(providerId);
        wd.setBankCardId(bankCardId);
        wd.setAmount(amount);
        withdrawalMapper.insert(wd);

        ProviderWalletFlow flow = new ProviderWalletFlow();
        flow.setProviderId(providerId);
        flow.setBizType("WITHDRAW_APPLY");
        flow.setAmount(-amount);
        flow.setBalanceAfter(w.getBalance() - amount);
        flow.setBizId(wd.getId());
        flow.setRemark("提现申请");
        flowMapper.insert(flow);

        return wd.getId();
    }

    @Override
    @Transactional
    public void approve(Long withdrawalId, Long adminId) {
        int rows = withdrawalMapper.markApproved(withdrawalId, adminId);
        if (rows == 0) throw new BaseException("提现单当前不可审批");
    }

    @Override
    @Transactional
    public void reject(Long withdrawalId, Long adminId, String reason) {
        if (reason == null || reason.isBlank()) throw new BaseException("请填写驳回原因");
        Withdrawal wd = withdrawalMapper.getById(withdrawalId);
        if (wd == null) throw new BaseException("提现单不存在");
        int rows = withdrawalMapper.markRejected(withdrawalId, adminId, reason);
        if (rows == 0) throw new BaseException("提现单当前不可驳回");

        ProviderWallet w = walletMapper.getById(wd.getProviderId());
        if (w != null) {
            walletMapper.refundBalance(wd.getProviderId(), wd.getAmount(), w.getVersion());
            ProviderWalletFlow flow = new ProviderWalletFlow();
            flow.setProviderId(wd.getProviderId());
            flow.setBizType("WITHDRAW_REJECT");
            flow.setAmount(wd.getAmount());
            flow.setBalanceAfter(w.getBalance() + wd.getAmount());
            flow.setBizId(withdrawalId);
            flow.setRemark("提现驳回退回");
            flowMapper.insert(flow);
        }
    }

    @Override
    @Transactional
    public void markPaid(Long withdrawalId, String transferNo) {
        int rows = withdrawalMapper.markPaid(withdrawalId, transferNo);
        if (rows == 0) throw new BaseException("提现单当前不可打款");

        Withdrawal wd = withdrawalMapper.getById(withdrawalId);
        ProviderWallet w = walletMapper.getById(wd.getProviderId());
        ProviderWalletFlow flow = new ProviderWalletFlow();
        flow.setProviderId(wd.getProviderId());
        flow.setBizType("WITHDRAW_PAID");
        flow.setAmount(-wd.getAmount());
        flow.setBalanceAfter(w == null ? 0 : w.getBalance());
        flow.setBizId(withdrawalId);
        flow.setRemark("提现打款完成");
        flowMapper.insert(flow);
    }
}
