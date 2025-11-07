package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "healthgoal")
public class HealthGoal {
    @Id
    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "goal_type", nullable = false, length = 64)
    private String goal_type;
    @Column(name = "target_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal target_weight;
    @Column(name = "target_date", nullable = false)
    private LocalDateTime target_date;
    @Column(name = "heat_target", nullable = false, precision = 10, scale = 2)
    private BigDecimal heat_target;

    /**
     * @OneToOne: `HealthGoal` 是关系拥有方。
     * @JoinColumn: `HealthGoal` 表包含 `user_id` 外键。
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
}
