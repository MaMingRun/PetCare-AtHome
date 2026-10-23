package com.mr.controller.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr.common.exception.BaseException;
import com.mr.common.result.Result;
import com.mr.service.support.PayLedger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付回调：路径不以 /user /admin /provider 开头，天然绕过 JWT 拦截器
 * 真实环境必须验签（密钥只有我和微信知道），练手阶段任何人可调 = 充当"微信服务器"
 */
@RestController
@RequestMapping("/callback")
public class PayCallbackController {

    @Autowired private PayLedger payLedger;
    @Autowired private ObjectMapper objectMapper;

    @PostMapping("/wechat/pay")
    public Result<Void> wechatPay(@RequestBody String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            String outTradeNo = node.path("outTradeNo").asText(null);
            String transactionId = node.path("transactionId").asText(null);
            if (outTradeNo == null || transactionId == null) {
                throw new BaseException("回调报文缺少必要字段");
            }
            payLedger.markPaid(outTradeNo, transactionId, raw);   //回调原文留档
            return Result.success();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("回调报文解析失败");
        }
    }
}
