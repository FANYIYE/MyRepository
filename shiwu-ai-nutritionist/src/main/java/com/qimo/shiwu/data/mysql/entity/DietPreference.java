package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dietpreference")
public class DietPreference {
    @Id
    @Column(name = "preference_id")
    private Long preferenceId;

    @Column(length = 64)
    private String allergens;
    @Column(length = 64)
    private String taboos;
    @Column(length = 64)
    private String taste_preference;
    @Column(length = 64)
    private String eating_habit;
    @Column(length = 64)
    private String equipment_limit;

    /**
     * @OneToOne: `DietPreference` 是关系拥有方。
     * @JoinColumn: `DietPreference` 表包含 `user_id` 外键。
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
}
