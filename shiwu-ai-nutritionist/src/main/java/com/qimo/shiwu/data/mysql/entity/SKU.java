package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "SKU")
public class SKU {

    @Id
    @Column(name = "sku_id")
    private Long skuId; // 使用 Long 对应 BIGINT

    @Column(name = "sku_code", unique = true, nullable = false, length = 64)
    private String skuCode;

    @Column(name = "sku_size", nullable = false, length = 64)
    private String skuSize;

    @Column(name = "sku_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal skuPrice;

    @Column(name = "sku_status", nullable = false)
    private Boolean skuStatus;

    @Column(name = "sku_heat", nullable = false, precision = 10, scale = 2)
    private BigDecimal skuHeat;

    /**
     * @ManyToOne: 多个 SKU 对应一个 Product。
     * @JoinColumn: `SKU` 表是关系的“拥有方”，它有 `product_id` 外键。
     */
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    /**
     * @OneToOne: 一个 SKU 对应一个库存条目。
     * @JoinColumn: `SKU` 表有 `inventory_id` 外键。
     */
    @OneToOne
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id")
    private Inventory inventory;
}
