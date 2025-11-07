package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "inventory_quantity", nullable = false)
    private Integer inventoryQuantity;

    @Column(name = "inventory_update_time", nullable = false)
    private LocalDateTime inventoryUpdateTime;
}
