package com.mr.service;

import com.mr.common.result.PageResult;
import com.mr.dto.OrderCreateDTO;
import com.mr.dto.PricePreviewDTO;
import com.mr.vo.OrderCreateVO;
import com.mr.vo.PricePreviewVO;
import com.mr.vo.UserOrderVO;

public interface OrderService {

    PricePreviewVO preview(PricePreviewDTO dto, Long userId);

    OrderCreateVO create(OrderCreateDTO dto, Long userId);

    /** 我的订单列表（状态过滤 + 分页） */
    PageResult<UserOrderVO> listMine(Long userId, String status, Integer page, Integer size);

    /** 我的订单详情（归属校验在 SQL 内） */
    UserOrderVO detailMine(Long id, Long userId);

    /** 取消订单（仅 PENDING_PAY 可取消） */
    void cancelMine(Long id, Long userId);
}
