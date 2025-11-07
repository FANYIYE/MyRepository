package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "reciperecord")
public class RecipeRecord {

    @Id
    @Column(name = "recipe_id")
    private Long recipeId; // 使用 Long 对应 BIGINT

    @Column(name = "recipe_cover_image_url", nullable = false, length = 256)
    private String recipeCoverImageUrl;

    @Column(name = "recipe_protein", nullable = false, precision = 10, scale = 2)
    private BigDecimal recipeProtein;

    @Column(name = "recipe_carbohydrate", nullable = false, precision = 10, scale = 2)
    private BigDecimal recipeCarbohydrate;

    @Column(name = "recipe_fat", nullable = false, precision = 10, scale = 2)
    private BigDecimal recipeFat;

    @Column(name = "recipe_fiber", nullable = false, precision = 10, scale = 2)
    private BigDecimal recipeFiber;

    @Column(name = "recipe_update_time", nullable = false)
    private LocalDateTime recipeUpdateTime;

    /**
     * @ManyToOne: 多个 RecipeRecord (食谱) 可以属于一个 User (用户)。
     */
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    /**
     * @ManyToOne: 多个 RecipeRecord (食谱) 可以属于一个 Order (订单)。
     */
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;

    /**
     * @ManyToMany: 一个食谱 (RecipeRecord) 包含多个 SKU (食材)，一个 SKU 也可以在多个食谱中。
     * @JoinTable: JPA 会通过中间表 `RecipeRecord_SKU` 来维护这个多对多关系。
     */
    @ManyToMany
    @JoinTable(
            name = "RecipeRecord_SKU",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "sku_id")
    )
    private List<SKU> skus;
}
