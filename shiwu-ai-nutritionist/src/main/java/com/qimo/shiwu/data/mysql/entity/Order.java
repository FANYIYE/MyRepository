package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "`Order`") // Order 是 SQL 关键字
public class Order {

    @Id
    @Column(name = "order_id")
    private Long orderId; // 使用 Long 对应 BIGINT

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "order_adress", nullable = false, length = 256)
    private String orderAdress;

    /**
     * @OneToMany: 一个 Order (订单) 可以包含多个 RecipeRecord (食谱)。
     */
    @OneToMany(mappedBy = "order")
    private List<RecipeRecord> recipeRecords;
}
