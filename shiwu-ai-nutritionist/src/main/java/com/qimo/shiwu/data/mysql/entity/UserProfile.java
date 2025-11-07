package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "userprofile")
public class UserProfile {

    @Id
    @Column(name = "profile_id")
    private Long profileId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal height;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;
    @Column(name = "activity_level", nullable = false, length = 4)
    private String activityLevel;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bmr;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tdee;
    @Column(name = "recipe_update_time", nullable = false)
    private LocalDateTime recipeUpdateTime;

    /**
     * @OneToOne: `UserProfile` 是关系拥有方。
     * @JoinColumn: `UserProfile` 表包含 `user_id` 外键。
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
}
