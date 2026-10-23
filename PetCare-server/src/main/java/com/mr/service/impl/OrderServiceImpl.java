package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.dto.OrderCreateDTO;
import com.mr.dto.PricePreviewDTO;
import com.mr.entity.Address;
import com.mr.entity.Orders;
import com.mr.entity.OrderDay;
import com.mr.entity.OrderPet;
import com.mr.entity.Pet;
import com.mr.entity.Service;
import com.mr.entity.ServicePriceRule;
import com.mr.mapper.AddressMapper;
import com.mr.mapper.OrderDayMapper;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.OrderPetMapper;
import com.mr.mapper.PetMapper;
import com.mr.mapper.ServiceMapper;
import com.mr.mapper.ServicePriceRuleMapper;
import com.mr.service.OrderService;
import com.mr.service.support.PriceCalculator;
import com.mr.common.result.PageResult;
import com.mr.vo.OrderCreateVO;
import com.mr.vo.PricePreviewVO;
import com.mr.vo.UserOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service   //全限定名，避让实体 Service
public class OrderServiceImpl implements OrderService {

    @Autowired private ServiceMapper serviceMapper;
    @Autowired private ServicePriceRuleMapper priceRuleMapper;
    @Autowired private PetMapper petMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderPetMapper orderPetMapper;
    @Autowired private OrderDayMapper orderDayMapper;
    @Autowired private PriceCalculator priceCalculator;

    @Override
    public PricePreviewVO preview(PricePreviewDTO dto, Long userId) {
        Service service = requireService(dto.getServiceId());
        int days = checkDays(dto.getDays());
        List<Pet> pets = requirePets(dto.getPetIds(), userId);
        List<ServicePriceRule> rules = priceRuleMapper.listEntityByServiceId(service.getId());
        return priceCalculator.calculate(service, rules, pets, days, dto.getDistanceKm());
    }

    @Override
    @Transactional   //三张表同生共死：任何异常整体回滚
    public OrderCreateVO create(OrderCreateDTO dto, Long userId) {
        //1. 校验服务
        Service service = requireService(dto.getServiceId());

        //2. 校验地址（归属在 SQL 内）
        Address address = addressMapper.getByIdAndUser(dto.getAddressId(), userId);
        if (address == null) {
            throw new BaseException("服务地址不存在");
        }

        //3. 校验宠物
        int days = checkDays(dto.getDays());
        List<Pet> pets = requirePets(dto.getPetIds(), userId);

        //4. 校验日期
        LocalDate startDate = dto.getStartDate();
        if (startDate == null || startDate.isBefore(LocalDate.now())) {
            throw new BaseException("预约日期不合法");
        }

        //5. 解析时段 "14:00-15:00"
        LocalTime[] slot = parseSlot(dto.getTimeSlot());

        //6. 服务端重新计价（不信前端金额）
        List<ServicePriceRule> rules = priceRuleMapper.listEntityByServiceId(service.getId());
        PricePreviewVO price = priceCalculator.calculate(service, rules, pets, days, dto.getDistanceKm());

        //7. 组装订单主表（地址 + 服务名全部快照）
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setServiceId(service.getId());
        order.setServiceName(service.getName());
        order.setStatus("PENDING_PAY");
        order.setStartDate(startDate);
        order.setDays(days);
        order.setTimeSlot(dto.getTimeSlot());
        order.setRemark(dto.getRemark());
        order.setContactName(address.getContactName());
        order.setContactPhone(address.getContactPhone());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setCommunity(address.getCommunity());
        order.setDetail(address.getDetail());
        order.setLongitude(address.getLongitude());
        order.setLatitude(address.getLatitude());
        order.setEntryMode(address.getEntryMode());
        order.setBaseAmount(price.getBaseAmount());
        order.setExtraAmount(price.getExtraAmount());
        order.setDiscountAmount(price.getDiscountAmount());
        order.setTotalAmount(price.getTotalAmount());
        order.setPayAmount(price.getTotalAmount());
        order.setPayExpireAt(LocalDateTime.now().plusMinutes(15)); //PRD：15分钟支付时限
        order.setAddressId(address.getId());
        order.setPayExpireAt(LocalDateTime.now().plusMinutes(15));   // 已有
        order.setDispatchDeadline(LocalDateTime.now().plusMinutes(15)); // ：15min 派单窗口
        order.setConfirmDeadline(LocalDateTime.now().plusDays(3));      //3 天内确认收货
        order.setReviewDeadline(LocalDateTime.now().plusDays(7));      //7 天内评价
        orderMapper.insert(order);  //自增 id 回填

        //8. 宠物快照
        for (Pet pet : pets) {
            OrderPet op = new OrderPet();
            op.setOrderId(order.getId());
            op.setPetId(pet.getId());
            op.setPetName(pet.getName());
            op.setSpecies(pet.getSpecies());
            orderPetMapper.insert(op);
        }

        //9. 按天拆子单
        for (int i = 0; i < days; i++) {
            OrderDay day = new OrderDay();
            day.setOrderId(order.getId());
            day.setDate(startDate.plusDays(i));
            day.setSlotBegin(slot[0]);
            day.setSlotEnd(slot[1]);
            orderDayMapper.insert(day);   //status 用数据库默认值 PENDING
        }

        //10. 返回支付所需信息
        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setPayAmount(order.getPayAmount());
        vo.setPayExpireAt(order.getPayExpireAt());
        return vo;
    }

    @Override
    public PageResult<UserOrderVO> listMine(Long userId, String status, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        int offset = (p - 1) * s;
        long total = orderMapper.countPageByUser(userId, status);
        List<Orders> list = orderMapper.listPageByUser(userId, status, offset, s);
        return new PageResult<>(list.stream().map(this::toUserVO).collect(Collectors.toList()),
                total, p, s);
    }

    @Override
    public UserOrderVO detailMine(Long id, Long userId) {
        Orders o = orderMapper.getByIdAndUser(id, userId);
        if (o == null) {
            throw new BaseException("订单不存在");
        }
        UserOrderVO v = toUserVO(o);
        v.setDayList(orderDayMapper.listByOrderId(id));
        return v;
    }

    @Override
    public void cancelMine(Long id, Long userId) {
        //归属 + 前态都在 SQL 内校验，影响0行 = 越权 / 已支付 / 已取消
        int rows = orderMapper.cancelByUser(id, userId);
        if (rows == 0) {
            throw new BaseException("订单不存在或当前状态不可取消");
        }
    }

    private UserOrderVO toUserVO(Orders o) {
        UserOrderVO v = new UserOrderVO();
        v.setId(o.getId());
        v.setOrderNo(o.getOrderNo());
        v.setServiceName(o.getServiceName());
        v.setStatus(o.getStatus());
        v.setStartDate(o.getStartDate());
        v.setDays(o.getDays());
        v.setTimeSlot(o.getTimeSlot());
        v.setRemark(o.getRemark());
        v.setContactName(o.getContactName());
        v.setContactPhone(o.getContactPhone());
        v.setAddress(catAddress(o));
        v.setEntryMode(o.getEntryMode());
        v.setBaseAmount(o.getBaseAmount());
        v.setExtraAmount(o.getExtraAmount());
        v.setDiscountAmount(o.getDiscountAmount());
        v.setTotalAmount(o.getTotalAmount());
        v.setPayAmount(o.getPayAmount());
        v.setPayExpireAt(o.getPayExpireAt());
        v.setCreatedAt(o.getCreatedAt());
        v.setCancelTime(o.getCancelTime());
        v.setCancelRemark(o.getCancelRemark());
        v.setPetNames(orderPetMapper.listByOrderId(o.getId())
                .stream().map(OrderPet::getPetName).collect(Collectors.toList()));
        return v;
    }

    private String catAddress(Orders o) {
        StringBuilder sb = new StringBuilder();
        if (o.getCity() != null) sb.append(o.getCity());
        if (o.getDistrict() != null) sb.append(o.getDistrict());
        if (o.getCommunity() != null) sb.append(o.getCommunity());
        if (o.getDetail() != null) sb.append(o.getDetail());
        return sb.toString();
    }

    // ---------- 私有校验方法 ----------

    private Service requireService(Long serviceId) {
        if (serviceId == null) {
            throw new BaseException("请选择服务");
        }
        Service service = serviceMapper.getOnShelfById(serviceId);
        if (service == null) {
            throw new BaseException("服务不存在或已下架");
        }
        return service;
    }

    private int checkDays(Integer days) {
        int d = days == null ? 1 : days;
        if (d < 1 || d > 30) {
            throw new BaseException("预约天数需在 1~30 天之间");
        }
        return d;
    }

    private List<Pet> requirePets(List<Long> petIds, Long userId) {
        if (petIds == null || petIds.isEmpty()) {
            throw new BaseException("请选择宠物");
        }
        List<Pet> pets = petMapper.listByIdsAndUser(petIds, userId);
        //查回来的数量和请求数量不一致 = 有 id 不存在或不属于当前用户
        if (pets.size() != petIds.size()) {
            throw new BaseException("部分宠物不存在，请刷新后重试");
        }
        return pets;
    }

    private LocalTime[] parseSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.matches("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$")) {
            throw new BaseException("服务时段格式不正确");
        }
        String[] parts = timeSlot.split("-");
        LocalTime begin = LocalTime.parse(parts[0]);
        LocalTime end = LocalTime.parse(parts[1]);
        if (!end.isAfter(begin)) {
            throw new BaseException("服务时段结束时间必须晚于开始时间");
        }
        return new LocalTime[]{begin, end};
    }

    private String generateOrderNo() {
        return "PC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                +  String.format("%03d", ThreadLocalRandom.current().nextInt(0, 1000));
    }
}
