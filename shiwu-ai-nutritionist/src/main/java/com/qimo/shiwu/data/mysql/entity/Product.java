package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Product")
public class Product {

    @Id
    @Column(name = "product_id")
    private Long productId; // 使用 Long 对应 BIGINT

    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    @Column(name = "product_picpath", length = 256)
    private String productPicpath;

    @Column(name = "product_desc", length = 256)
    private String productDesc;

    @Column(name = "product_create_time", nullable = false)
    private LocalDateTime productCreateTime;

    /**
     * @OneToMany: 一个 Product (产品，如 "牛肉") 对应多个 SKU (规格，如 "500g", "1kg")。
     * `mappedBy = "product"`: 由 `SKU` 类的 `product` 字段维护。
     */
    @OneToMany(mappedBy = "product")
    private List<SKU> skus;
}
