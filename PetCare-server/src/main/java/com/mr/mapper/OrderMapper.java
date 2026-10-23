package com.mr.mapper;

import com.mr.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Orders order);

    //根据订单主键id查询订单
    Orders getById(Long id);

    //根据订单id + 用户id查询（做权限控制：只能查自己的订单）
    Orders getByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    //根据业务订单号 orderNo 查询订单
    Orders getByOrderNo(String orderNo);

    //修改订单状态为已支付
    int markPaid(@Param("orderNo") String orderNo,
                 @Param("fromStatus") String fromStatus,
                 @Param("toStatus") String toStatus);

    //管理端：按状态查询订单（派单池用，待处理时间最早的在前）
    List<Orders> listByStatus(@Param("status") String status);

    //服务师接单：条件更新防并发抢单，只有还是待接单才允许成功
    int markAccepted(@Param("orderId") Long orderId,
                     @Param("fromStatus") String fromStatus,
                     @Param("toStatus") String toStatus,
                     @Param("providerId") Long providerId);

    //派单串行化锚点：锁住订单行，让同订单的并发派单在这里排队
    Long lockById(Long orderId);

    //履约状态推进：只有前态匹配且是自己的单才更新成功
    int updateStatus(@Param("id") Long id,
                     @Param("from") String from,
                     @Param("to") String to,
                     @Param("providerId") Long providerId);

    // 用户确认收货：只卡前态 PENDING_CONFIRM，不卡 providerId
    int confirmByUser(@Param("id") Long id);

    // --- 管理端分页 ---
    List<Orders> listPage(@Param("status") String status,
                          @Param("keyword") String keyword,
                          @Param("offset") int offset,
                          @Param("size") int size);
    long countPage(@Param("status") String status,
                   @Param("keyword") String keyword);

    // --- Dashboard 统计 ---
    long countByStatus(@Param("status") String status);
    long countToday();                        // 今日新增
    long sumPayAmountPaid();                  // 已支付 GMV（分）
    long countTotalOrders();                  // 全部订单数
    long countPaidAndAfter();                 // 已支付及之后的订单数（PENDING_ACCEPT 及之后）


    // --- 定时任务：超时扫描 + 条件更新
    /** 支付超时：PENDING_PAY 且 pay_expire_at < now() */
    List<Orders> listPayExpired();

    /** 派单超时：PENDING_ACCEPT 且 dispatch_deadline < now() */
    List<Orders> listDispatchExpired();

    /** 确认/评价超时：PENDING_CONFIRM 且 confirm_deadline < now() */
    List<Orders> listConfirmExpired();

    /** 订单支付超时 → CANCELLED（只卡前态，防重复跑） */
    int markPayCancelled(@Param("id") Long id);

    /** 派单超时 → CANCELLED */
    int markDispatchCancelled(@Param("id") Long id);

    /** 确认超时 → SETTLING（自动进入评价阶段，用户默认放弃确认权） */
    int markConfirmAutoSettling(@Param("id") Long id);

    /** 评价超时 → FINISHED（自动好评） */
    int markReviewAutoFinished(@Param("id") Long id);

    /** 评价超时批量 → FINISHED */
    int batchReviewAutoFinished();

    // --- 用户端：我的订单 ---
    List<Orders> listPageByUser(@Param("userId") Long userId,
                                @Param("status") String status,
                                @Param("offset") int offset,
                                @Param("size") int size);
    long countPageByUser(@Param("userId") Long userId,
                         @Param("status") String status);

    /** 用户取消未支付订单：同时卡订单归属和前态 PENDING_PAY，越权/重复取消都影响0行 */
    int cancelByUser(@Param("id") Long id, @Param("userId") Long userId);

    // --- 服务师端：我的订单 ---
    List<Orders> listByProvider(@Param("providerId") Long providerId,
                                @Param("status") String status);

    /** 服务师订单详情：归属校验在 SQL 内 */
    Orders getByIdAndProvider(@Param("id") Long id, @Param("providerId") Long providerId);
}
