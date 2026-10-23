package com.mr.controller.admin;

import com.mr.common.context.BaseContext;
import com.mr.common.result.PageResult;
import com.mr.common.result.Result;
import com.mr.dto.AdminDispatchDTO;
import com.mr.dto.PageQueryDTO;
import com.mr.entity.Provider;
import com.mr.mapper.ProviderMapper;
import com.mr.service.AdminDashboardService;
import com.mr.service.DispatchService;
import com.mr.service.SettlementSettleService;
import com.mr.service.WithdrawalService;
import com.mr.vo.AdminOrderVO;
import com.mr.vo.AdminWithdrawalVO;
import com.mr.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminOrderController {

    @Autowired private DispatchService dispatchService;
    @Autowired private SettlementSettleService settleService;
    @Autowired private WithdrawalService withdrawalService;
    @Autowired private AdminDashboardService dashboardService;
    @Autowired private ProviderMapper providerMapper;

    // === 仪表盘 ===
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(dashboardService.getDashboard());
    }

    // === 订单管理 ===
    /** 活跃服务师下拉列表（派单 Dialog 用） */
    @GetMapping("/providers")
    public Result<List<Provider>> activeProviders() {
        return Result.success(providerMapper.listActive());
    }
    /** 待派单订单池 */
    @GetMapping("/orders/pending-accept")
    public Result<List<AdminOrderVO>> pendingAccept() {
        return Result.success(dispatchService.listPendingAccept());
    }
    /** 订单分页列表（状态+keyword 过滤） */
    @GetMapping("/orders/page")
    public Result<PageResult<AdminOrderVO>> ordersPage(PageQueryDTO q) {
        return Result.success(dashboardService.ordersPage(q));
    }
    /** 订单详情 */
    @GetMapping("/orders/{id}")
    public Result<AdminOrderVO> orderDetail(@PathVariable Long id) {
        return Result.success(dashboardService.orderDetail(id));
    }
    /** 指派服务师 */
    @PostMapping("/orders/{id}/dispatch")
    public Result<Void> dispatch(@PathVariable Long id, @RequestBody AdminDispatchDTO dto) {
        dispatchService.dispatch(id, dto.getProviderId(), BaseContext.getCurrentId(), dto.getRemark());
        return Result.success();
    }

    // === 结算 ===
    /** 手动触发 T+1 结算 */
    @PostMapping("/settlements/settle-all")
    public Result<Integer> settleAll() {
        return Result.success(settleService.settleAll());
    }

    // === 提现管理 ===
    /** 提现单分页列表 */
    @GetMapping("/withdrawals/page")
    public Result<PageResult<AdminWithdrawalVO>> withdrawalsPage(PageQueryDTO q) {
        return Result.success(dashboardService.withdrawalsPage(q));
    }
    /** 审批通过提现 */
    @PostMapping("/withdrawals/{id}/approve")
    public Result<Void> approveWithdraw(@PathVariable Long id) {
        withdrawalService.approve(id, BaseContext.getCurrentId());
        return Result.success();
    }
    /** 驳回提现 */
    @PostMapping("/withdrawals/{id}/reject")
    public Result<Void> rejectWithdraw(@PathVariable Long id, @RequestBody Map<String,String> body) {
        withdrawalService.reject(id, BaseContext.getCurrentId(), body.get("reason"));
        return Result.success();
    }
    /** 标记提现打款完成 */
    @PostMapping("/withdrawals/{id}/paid")
    public Result<Void> markWithdrawPaid(@PathVariable Long id, @RequestBody Map<String,String> body) {
        withdrawalService.markPaid(id, body.get("transferNo"));
        return Result.success();
    }
}
