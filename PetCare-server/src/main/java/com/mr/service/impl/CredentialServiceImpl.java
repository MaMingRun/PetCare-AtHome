package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.common.utils.CryptoUtil;
import com.mr.entity.Address;
import com.mr.entity.EntryCredentialLog;
import com.mr.entity.Orders;
import com.mr.enums.OrderStatus;
import com.mr.mapper.AddressMapper;
import com.mr.mapper.EntryCredentialLogMapper;
import com.mr.mapper.OrderMapper;
import com.mr.service.CredentialService;
import com.mr.vo.CredentialLogVO;
import com.mr.vo.CredentialVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// 注意：用全限定名 @org.springframework.stereotype.Service 会跟实体 Service 冲突，
// 所以改用 import 进来的 Service（但前面要加别名？不，直接用就行，因为 Service 是 Spring 的，实体 Service 没被 import）
@Service
public class CredentialServiceImpl implements CredentialService {

    /** 服务师可查看凭证的状态：到达后 → 当天完成前 */
    private static final Set<String> PROVIDER_ALLOWED_STATUSES = Set.of(
            OrderStatus.ARRIVED.name(),
            OrderStatus.SERVING.name()
    );

    private static final String ACT_VIEW = "VIEW";
    private static final String ACT_UNLOCK = "UNLOCK";

    @Autowired private OrderMapper orderMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private EntryCredentialLogMapper logMapper;
    @Autowired private CryptoUtil cryptoUtil;

    @Override
    @Transactional
    public CredentialVO view(Long orderId, Long providerId, String ip) {
        // 1. 校验订单归属 + 状态闸门
        Orders order = requireMyOrder(orderId, providerId);
        if (!PROVIDER_ALLOWED_STATUSES.contains(order.getStatus())) {
            throw new BaseException("仅在到达后、当天服务完成前可查看入户凭证");
        }

        // 2. AT_HOME 直接返回提示，不查凭证、不写日志
        if ("AT_HOME".equals(order.getEntryMode())) {
            CredentialVO vo = new CredentialVO();
            vo.setEntryMode("AT_HOME");
            vo.setTip("主人在家，无需入户凭证");
            return vo;
        }

        // 3. 查 address 取密文 → 解密
        Long addressId = order.getAddressId();
        if (addressId == null) {
            throw new BaseException("订单缺少地址关联，无法获取凭证");
        }
        Address address = addressMapper.getById(addressId);
        if (address == null || address.getEntrySecretCipher() == null) {
            throw new BaseException("该地址未设置入户凭证");
        }
        String plain = cryptoUtil.decrypt(address.getEntrySecretCipher());

        // 4. 写 VIEW 日志
        writeLog(order, addressId, ACT_VIEW, ip);

        // 5. 返回
        CredentialVO vo = new CredentialVO();
        vo.setEntryMode(order.getEntryMode());
        vo.setSecret(plain);
        vo.setTip("请妥善保管，勿泄露给他人");
        return vo;
    }

    @Override
    @Transactional
    public void unlock(Long orderId, Long providerId, String ip) {
        Orders order = requireMyOrder(orderId, providerId);
        if (!PROVIDER_ALLOWED_STATUSES.contains(order.getStatus())) {
            throw new BaseException("仅在到达后、当天服务完成前可使用入户凭证");
        }
        if ("AT_HOME".equals(order.getEntryMode())) {
            throw new BaseException("主人在家，无需开锁操作");
        }
        // 真实场景这里会调智能锁网关，本步仅写 UNLOCK 日志
        writeLog(order, order.getAddressId(), ACT_UNLOCK, ip);
    }

    @Override
    public List<CredentialLogVO> listByOrder(Long orderId, Long userId) {
        Orders order = orderMapper.getByIdAndUser(orderId, userId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        return logMapper.listByOrder(orderId).stream().map(this::toVO).collect(Collectors.toList());
    }

    // ---------- 私有方法 ----------

    /** 服务师端：必须是我的单，否则拒绝 */
    private Orders requireMyOrder(Long orderId, Long providerId) {
        Orders order = orderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!providerId.equals(order.getProviderId())) {
            throw new BaseException("不是你的订单");
        }
        return order;
    }

    /** 写 entry_credential_log 一条 */
    private void writeLog(Orders order, Long addressId, String action, String ip) {
        EntryCredentialLog log = new EntryCredentialLog();
        log.setOrderId(order.getId());
        log.setAddressId(addressId);
        log.setProviderId(order.getProviderId());
        log.setAction(action);
        log.setIp(ip);
        logMapper.insert(log);
    }

    /** 实体 → VO（字段一一对应） */
    private CredentialLogVO toVO(EntryCredentialLog log) {
        CredentialLogVO vo = new CredentialLogVO();
        vo.setId(log.getId());
        vo.setAction(log.getAction());
        vo.setProviderId(log.getProviderId());
        vo.setIp(log.getIp());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}