package com.mr.controller.user;

import com.mr.common.context.BaseContext;
import com.mr.common.result.PageResult;
import com.mr.common.result.Result;
import com.mr.dto.OrderCreateDTO;
import com.mr.dto.PricePreviewDTO;
import com.mr.dto.ReviewCreateDTO;
import com.mr.service.OrderService;
import com.mr.service.UserReviewService;
import com.mr.vo.OrderCreateVO;
import com.mr.vo.PricePreviewVO;
import com.mr.vo.ReviewVO;
import com.mr.vo.UserOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.mr.service.CredentialService;
import com.mr.vo.CredentialLogVO;
import java.util.List;
import com.mr.service.ConfirmService;

@RestController
@RequestMapping("/user/orders")
public class UserOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private ConfirmService confirmService;
    @Autowired
    private UserReviewService reviewService;

    /** 我的订单列表（状态过滤 + 分页） */
    @GetMapping
    public Result<PageResult<UserOrderVO>> list(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size) {
        return Result.success(orderService.listMine(BaseContext.getCurrentId(), status, page, size));
    }

    /** 我的订单详情 */
    @GetMapping("/{id}")
    public Result<UserOrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detailMine(id, BaseContext.getCurrentId()));
    }

    /** 取消订单（仅待支付可取消） */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelMine(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /** 价格试算 */
    @PostMapping("/price-preview")
    public Result<PricePreviewVO> preview(@RequestBody PricePreviewDTO dto) {
        return Result.success(orderService.preview(dto, BaseContext.getCurrentId()));
    }

    /** 创建订单（待支付） */
    @PostMapping
    public Result<OrderCreateVO> create(@RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(dto, BaseContext.getCurrentId()));
    }

    /** 透明展示：查看我订单的入户凭证操作日志 */
    @GetMapping("/{id}/credential-logs")
    public Result<List<CredentialLogVO>> credentialLogs(@PathVariable Long id) {
        return Result.success(credentialService.listByOrder(id, BaseContext.getCurrentId()));
    }

    /** 用户确认收货（PENDING_CONFIRM → SETTLING） */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        confirmService.confirm(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /** 用户评价（SETTLING → REVIEWED） */
    @PostMapping("/{id}/review")
    public Result<ReviewVO> review(@PathVariable Long id, @RequestBody ReviewCreateDTO dto) {
        return Result.success(reviewService.submit(id, BaseContext.getCurrentId(), dto));
    }
}
