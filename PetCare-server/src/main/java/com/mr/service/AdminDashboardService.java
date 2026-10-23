package com.mr.service;
import com.mr.common.result.PageResult;
import com.mr.dto.PageQueryDTO;
import com.mr.vo.AdminOrderVO;
import com.mr.vo.AdminWithdrawalVO;
import com.mr.vo.DashboardVO;

public interface AdminDashboardService {
    DashboardVO getDashboard();
    PageResult<AdminOrderVO> ordersPage(PageQueryDTO q);
    PageResult<AdminWithdrawalVO> withdrawalsPage(PageQueryDTO q);
    AdminOrderVO orderDetail(Long id);
}
