package com.mr.controller.admin;

import com.mr.common.exception.BaseException;
import com.mr.common.result.PageResult;
import com.mr.common.result.Result;
import com.mr.dto.PageQueryDTO;
import com.mr.entity.EntryCredentialLog;
import com.mr.entity.OrderStatusLog;
import com.mr.entity.Provider;
import com.mr.entity.ProviderWalletFlow;
import com.mr.entity.Review;
import com.mr.entity.Service;
import com.mr.entity.User;
import com.mr.mapper.EntryCredentialLogMapper;
import com.mr.mapper.OrderStatusLogMapper;
import com.mr.mapper.ProviderMapper;
import com.mr.mapper.ProviderWalletFlowMapper;
import com.mr.mapper.ReviewMapper;
import com.mr.mapper.ServiceMapper;
import com.mr.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端 - 通用管理能力
 * 覆盖：服务师管理 / 钱包流水 / 评价管理 / 订单日志 / 用户管理 / 服务项目 CRUD
 */
@RestController
@RequestMapping("/admin")
public class AdminManageController {

    @Autowired private ProviderMapper providerMapper;
    @Autowired private ProviderWalletFlowMapper walletFlowMapper;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private OrderStatusLogMapper statusLogMapper;
    @Autowired private EntryCredentialLogMapper credentialLogMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ServiceMapper serviceMapper;

    // ==================== 服务师管理 ====================

    @GetMapping("/providers/page")
    public Result<PageResult<Provider>> providersPage(PageQueryDTO q) {
        int offset = (q.getPage() - 1) * q.getSize();
        List<Provider> list = providerMapper.listPage(q.getStatus(), q.getKeyword(), offset, q.getSize());
        long total = providerMapper.countPage(q.getStatus(), q.getKeyword());
        return Result.success(new PageResult<>(list, total, q.getPage(), q.getSize()));
    }

    @GetMapping("/providers/{id}")
    public Result<Provider> providerDetail(@PathVariable Long id) {
        Provider p = providerMapper.getById(id);
        if (p == null) throw new BaseException("服务师不存在");
        // 脱敏身份证号
        p.setIdCardNoCipher(null);
        return Result.success(p);
    }

    /** 修改服务师状态：ACTIVE / SUSPENDED / BANNED / QUIT */
    @PostMapping("/providers/{id}/status")
    public Result<Void> providerUpdateStatus(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isEmpty()) throw new BaseException("状态不能为空");
        int rows = providerMapper.updateStatus(id, status);
        if (rows == 0) throw new BaseException("服务师不存在或已删除");
        return Result.success();
    }

    // ==================== 钱包流水 ====================

    @GetMapping("/wallet/flows/page")
    public Result<PageResult<Map<String, Object>>> walletFlowsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String bizType) {
        int offset = (page - 1) * size;
        List<ProviderWalletFlow> list = walletFlowMapper.listPage(providerId, bizType, offset, size);
        long total = walletFlowMapper.countPage(providerId, bizType);

        // 拼接服务师姓名
        Map<Long, String> nameCache = new HashMap<>();
        List<Map<String, Object>> enriched = list.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("providerId", f.getProviderId());
            m.put("providerName", nameCache.computeIfAbsent(f.getProviderId(), pid -> {
                Provider p = providerMapper.getById(pid);
                return p == null ? "未知" : p.getRealName();
            }));
            m.put("bizType", f.getBizType());
            m.put("amount", f.getAmount());
            m.put("balanceAfter", f.getBalanceAfter());
            m.put("bizId", f.getBizId());
            m.put("remark", f.getRemark());
            m.put("createdAt", f.getCreatedAt());
            return m;
        }).toList();

        return Result.success(new PageResult<>(enriched, total, page, size));
    }

    // ==================== 评价管理 ====================

    @GetMapping("/reviews/page")
    public Result<PageResult<Review>> reviewsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Integer minScore) {
        int offset = (page - 1) * size;
        List<Review> list = reviewMapper.listPage(providerId, minScore, offset, size);
        long total = reviewMapper.countPage(providerId, minScore);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @GetMapping("/reviews/stat")
    public Result<Map<String, Object>> reviewStat(@RequestParam(required = false) Long providerId) {
        Map<String, Object> stat = reviewMapper.statSummary(providerId);
        return Result.success(stat == null ? new HashMap<>() : stat);
    }

    // ==================== 订单状态日志 + 入户凭证 ====================

    @GetMapping("/orders/{orderId}/status-logs")
    public Result<List<OrderStatusLog>> orderStatusLogs(@PathVariable Long orderId) {
        return Result.success(statusLogMapper.listByOrder(orderId));
    }

    @GetMapping("/orders/{orderId}/credential-logs")
    public Result<List<EntryCredentialLog>> orderCredentialLogs(@PathVariable Long orderId) {
        return Result.success(credentialLogMapper.listByOrder(orderId));
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users/page")
    public Result<PageResult<User>> usersPage(PageQueryDTO q) {
        int offset = (q.getPage() - 1) * q.getSize();
        List<User> list = userMapper.listPage(q.getKeyword(), q.getStatus(), offset, q.getSize());
        long total = userMapper.countPage(q.getKeyword(), q.getStatus());
        // 手机号脱敏
        list.forEach(u -> {
            if (u.getPhone() != null && u.getPhone().length() == 11) {
                u.setPhone(u.getPhone().substring(0, 3) + "****" + u.getPhone().substring(7));
            }
        });
        return Result.success(new PageResult<>(list, total, q.getPage(), q.getSize()));
    }

    @GetMapping("/users/{id}")
    public Result<User> userDetail(@PathVariable Long id) {
        User u = userMapper.getById(id);
        if (u == null) throw new BaseException("用户不存在");
        return Result.success(u);
    }

    /** 拉黑 / 取消拉黑 */
    @PostMapping("/users/{id}/blacklist")
    public Result<Void> userBlacklist(@PathVariable Long id,
                                      @RequestBody Map<String, Object> body) {
        Integer blacklisted = (Integer) body.get("blacklisted");
        String reason = (String) body.getOrDefault("reason", "");
        if (blacklisted == null) throw new BaseException("blacklisted 必填");
        int rows = userMapper.updateBlacklist(id, blacklisted, reason);
        if (rows == 0) throw new BaseException("用户不存在");
        return Result.success();
    }

    // ==================== 服务项目 CRUD ====================

    @GetMapping("/services/page")
    public Result<PageResult<Service>> servicesPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        int offset = (page - 1) * size;
        List<Service> list = serviceMapper.listPage(status, category, keyword, offset, size);
        long total = serviceMapper.countPage(status, category, keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @GetMapping("/services/{id}")
    public Result<Service> serviceDetail(@PathVariable Long id) {
        Service s = serviceMapper.getById(id);
        if (s == null) throw new BaseException("服务不存在");
        return Result.success(s);
    }

    @PostMapping("/services")
    public Result<Long> serviceCreate(@RequestBody Service service) {
        if (service.getName() == null || service.getName().isEmpty()) throw new BaseException("服务名必填");
        if (service.getStatus() == null) service.setStatus("OFF");
        if (service.getSort() == null) service.setSort(0);
        serviceMapper.insertService(service);
        return Result.success(service.getId());
    }

    @PutMapping("/services/{id}")
    public Result<Void> serviceUpdate(@PathVariable Long id, @RequestBody Service service) {
        service.setId(id);
        int rows = serviceMapper.updateService(service);
        if (rows == 0) throw new BaseException("服务不存在或已删除");
        return Result.success();
    }

    @PostMapping("/services/{id}/status")
    public Result<Void> serviceUpdateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (!"ON".equals(status) && !"OFF".equals(status)) {
            throw new BaseException("status 只能是 ON 或 OFF");
        }
        int rows = serviceMapper.updateStatus(id, status);
        if (rows == 0) throw new BaseException("服务不存在");
        return Result.success();
    }
}
