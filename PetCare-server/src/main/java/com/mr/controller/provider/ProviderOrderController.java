package com.mr.controller.provider;

import com.mr.common.context.BaseContext;
import com.mr.common.result.Result;
import com.mr.dto.CheckinDTO;
import com.mr.dto.ProviderRejectDTO;
import com.mr.dto.RecordCreateDTO;
import com.mr.dto.WithdrawApplyDTO;
import com.mr.entity.ProviderWalletFlow;
import com.mr.service.DispatchService;
import com.mr.service.FulfillService;
import com.mr.service.WalletQueryService;
import com.mr.service.WithdrawalService;
import com.mr.vo.ProviderDispatchVO;
import com.mr.vo.ProviderOrderVO;
import com.mr.vo.WalletVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mr.service.CredentialService;
import com.mr.vo.CredentialVO;
import jakarta.servlet.http.HttpServletRequest;
import com.mr.service.ReportService;
import com.mr.dto.ReportCreateDTO;
import com.mr.vo.ReportVO;

import java.util.List;

@RestController
@RequestMapping("/provider")
public class ProviderOrderController {

    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private FulfillService fulfillService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private WalletQueryService walletService;
    @Autowired
    private WithdrawalService withdrawalService;


    /** 我的待响应派单 */
    @GetMapping("/dispatches")
    public Result<List<ProviderDispatchVO>> myDispatches() {
        return Result.success(dispatchService.listMine(BaseContext.getCurrentId()));
    }

    /** 我的服务订单列表（按状态过滤） */
    @GetMapping("/orders")
    public Result<List<ProviderOrderVO>> myOrders(@RequestParam(required = false) String status) {
        return Result.success(fulfillService.listProviderOrders(BaseContext.getCurrentId(), status));
    }

    /** 我的服务订单详情 */
    @GetMapping("/orders/{id}")
    public Result<ProviderOrderVO> orderDetail(@PathVariable Long id) {
        return Result.success(fulfillService.getProviderOrderDetail(id, BaseContext.getCurrentId()));
    }

    /** 接单 */
    @PostMapping("/orders/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        dispatchService.accept(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /** 拒单 */
    @PostMapping("/orders/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody ProviderRejectDTO dto) {
        dispatchService.reject(id, BaseContext.getCurrentId(), dto.getRemark());
        return Result.success();
    }



    /** 出发 */
    @PostMapping("/orders/{id}/depart")
    public Result<Void> depart(@PathVariable Long id) {
        fulfillService.depart(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /** 到达（打卡：上传定位，写 CHECKIN 留痕） */
    @PostMapping("/orders/{id}/arrive")
    public Result<Void> arrive(@PathVariable Long id, @RequestBody CheckinDTO dto) {
        fulfillService.arrive(id, BaseContext.getCurrentId(), dto);
        return Result.success();
    }

    /** 开始服务 */
    @PostMapping("/orders/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        fulfillService.start(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /** 完成当天 */
    @PostMapping("/orders/{id}/finish-day")
    public Result<Void> finishDay(@PathVariable Long id) {
        fulfillService.finishDay(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /** 服务过程留痕（PHOTO/VIDEO/NOTE） */
    @PostMapping("/orders/{id}/records")
    public Result<Void> addRecord(@PathVariable Long id, @RequestBody RecordCreateDTO dto) {
        fulfillService.addRecord(id, BaseContext.getCurrentId(), dto);
        return Result.success();
    }

    /** 查看入户凭证（仅 ARRIVED/SERVING 状态允许） */
    @GetMapping("/orders/{id}/credential")
    public Result<CredentialVO> viewCredential(@PathVariable Long id, HttpServletRequest req) {
        return Result.success(credentialService.view(id, BaseContext.getCurrentId(), getClientIp(req)));
    }

    /** 使用入户凭证（模拟开锁，仅写日志） */
    @PostMapping("/orders/{id}/credential/unlock")
    public Result<Void> unlockCredential(@PathVariable Long id, HttpServletRequest req) {
        credentialService.unlock(id, BaseContext.getCurrentId(), getClientIp(req));
        return Result.success();
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /** 服务师提交每日服务报告 */
    @PostMapping("/orders/{id}/report")
    public Result<ReportVO> submitReport(@PathVariable Long id, @RequestBody ReportCreateDTO dto) {
        return Result.success(reportService.submit(id, BaseContext.getCurrentId(), dto));
    }

    /** 查我的钱包 */
    @GetMapping("/wallet")
    public Result<WalletVO> wallet() {
        return Result.success(walletService.getWallet(BaseContext.getCurrentId()));
    }

    /** 查钱包流水 */
    @GetMapping("/wallet/flows")
    public Result<List<ProviderWalletFlow>> flows() {
        return Result.success(walletService.listFlows(BaseContext.getCurrentId()));
    }

    /** 申请提现 */
    @PostMapping("/withdraw")
    public Result<Long> applyWithdraw(@RequestBody WithdrawApplyDTO dto) {
        return Result.success(withdrawalService.apply(BaseContext.getCurrentId(), dto.getAmount(), dto.getBankCardId()));
    }
}
