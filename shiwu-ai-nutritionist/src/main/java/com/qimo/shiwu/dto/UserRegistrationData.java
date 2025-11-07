package com.qimo.shiwu.dto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) - 数据传输对象
 *
 * [开发者注意]:
 * 这个类用于封装“用户注册”时从前端接收到的所有数据。
 * `UserService` 的 `registerNewUser` 方法会接收这个对象。
 */
@Data
public class UserRegistrationData {

    // --- User ---
    private String username;
    private String password; // (实际项目中应传输加密后的)

    // --- UserProfile ---
    private BigDecimal height;
    private BigDecimal weight;
    private String activityLevel;
    // BMR 和 TDEE 将在服务端计算，不需要传入

    // --- DietPreference ---
    private String allergens;
    private String taboos;
    private String taste_preference;
    private String eating_habit;
    private String equipment_limit;

    // --- HealthGoal ---
    private String goalType;
    private BigDecimal target_weight;
    private LocalDateTime target_date;
    private BigDecimal heat_target;
}
