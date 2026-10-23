package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单-宠物关联（含下单时快照）
 */
@Data
public class OrderPet {

    private Long id;                 // 主键ID
    private Long orderId;            // 订单ID，关联Orders表，一个订单可以关联多条OrderPet（多只宠物）
    private Long petId;              // 用户宠物档案ID，关联Pet表
    private String petName;          // 快照：下单那一刻宠物名字
    private String species;          // 快照：下单那一刻宠物物种 CAT / DOG / OTHER
    private LocalDateTime createdAt; // 记录创建时间
}
