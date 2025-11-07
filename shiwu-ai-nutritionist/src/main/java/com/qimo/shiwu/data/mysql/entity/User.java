package com.qimo.shiwu.data.mysql.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "`User`") // User 是 SQL 关键字
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId; // 对应 BIGINT (雪花算法)

    @Column(name = "user_name", unique = true, nullable = false, length = 32)
    private String userName;

    @Column(name = "user_password", length = 128)
    private String userPassword;

    // --- 关系映射 (已修正) ---

    /**
     * @OneToOne(mappedBy = "user"):
     * 关系被 `UserProfile` 类的 `user` 字段所维护。
     * `User` 表不再包含 `profile_id`。
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile;

    /**
     * @OneToOne(mappedBy = "user"):
     * 关系被 `DietPreference` 类的 `user` 字段所维护。
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private DietPreference dietPreference;

    /**
     * @OneToOne(mappedBy = "user"):
     * 关系被 `HealthGoal` 类的 `user` 字段所维护。
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private HealthGoal healthGoal;

    /**
     * @OneToMany: 定义与 RecipeRecord 的一对多关系。
     * `mappedBy = "user"`: 指明这个关系是由 `RecipeRecord` 类中的 `user` 字段来维护的。
     */
    @OneToMany(mappedBy = "user")
    private List<RecipeRecord> recipeRecords;
}
