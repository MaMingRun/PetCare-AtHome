package com.mr.controller.user;

import com.mr.common.context.BaseContext;
import com.mr.common.result.Result;
import com.mr.service.PayService;
import com.mr.vo.OrderPayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/orders")
public class UserPayController {

    @Autowired
    private PayService payService;

    /** 支付订单（Mock 模式下调完即支付成功） */
    @PostMapping("/{id}/pay")
    public Result<OrderPayVO> pay(@PathVariable Long id) {
        return Result.success(payService.pay(id, BaseContext.getCurrentId()));
    }
}
