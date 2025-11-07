package com.qimo.shiwu.service;
import com.qimo.shiwu.data.mysql.entity.DietPreference;
import com.qimo.shiwu.data.mysql.entity.HealthGoal;
import com.qimo.shiwu.data.mysql.entity.User;
import com.qimo.shiwu.data.mysql.entity.UserProfile;
import com.qimo.shiwu.data.mysql.repository.DietPreferenceRepository;
import com.qimo.shiwu.data.mysql.repository.HealthGoalRepository;
import com.qimo.shiwu.data.mysql.repository.UserProfileRepository;
import com.qimo.shiwu.data.mysql.repository.UserRepository;
import com.qimo.shiwu.dto.UserRegistrationData;
import com.qimo.shiwu.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ===================================================================
 * 服务 1: 用户服务 (处理用户注册、登录、信息查询)
 * ===================================================================
 * [开发者注意]:
 * 封装所有用户相关的业务逻辑。
 */
@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private DietPreferenceRepository dietPreferenceRepository;
    @Autowired private HealthGoalRepository healthGoalRepository;
    @Autowired private SnowflakeIdGenerator idGenerator;

    /**
     * [核心功能]: 封装《记录文档》中的复杂注册流程
     * 后续开发者调用此方法即可完成新用户的完整创建。
     * @Transactional 保证所有数据库操作要么全部成功，要么全部失败。
     */
    @Transactional
    public User registerNewUser(UserRegistrationData data) {

        // 1. 检查用户名是否已存在
        if (userRepository.findByUserName(data.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建并保存子表实体 (此时 user_id 为 null)
        // (在实际应用中，BMR 和 TDEE 应通过算法计算)
        UserProfile profile = new UserProfile();
        profile.setProfileId(idGenerator.nextId());
        profile.setHeight(data.getHeight());
        profile.setWeight(data.getWeight());
        profile.setActivityLevel(data.getActivityLevel());
        profile.setBmr(new BigDecimal("1800.00")); // 示例 BMR
        profile.setTdee(new BigDecimal("2200.00")); // 示例 TDEE
        profile.setRecipeUpdateTime(LocalDateTime.now());
        userProfileRepository.save(profile);

        DietPreference preference = new DietPreference();
        preference.setPreferenceId(idGenerator.nextId());
        preference.setAllergens(data.getAllergens());
        preference.setTaboos(data.getTaboos());
        preference.setTaste_preference(data.getTaste_preference());
        preference.setEating_habit(data.getEating_habit());
        preference.setEquipment_limit(data.getEquipment_limit());
        dietPreferenceRepository.save(preference);

        HealthGoal goal = new HealthGoal();
        goal.setGoalId(idGenerator.nextId());
        goal.setGoal_type(data.getGoalType());
        goal.setTarget_weight(data.getTarget_weight());
        goal.setTarget_date(data.getTarget_date());
        goal.setHeat_target(data.getHeat_target());
        healthGoalRepository.save(goal);

        // 3. 创建并保存主表 User
        User user = new User();
        user.setUserId(idGenerator.nextId());
        user.setUserName(data.getUsername());
        user.setUserPassword(data.getPassword()); // (密码应加密)
        user.setUserProfile(profile);
        user.setDietPreference(preference);
        user.setHealthGoal(goal);
        userRepository.save(user);

        // 4. 回填子表中的 user_id 外键
        profile.setUser(user);
        preference.setUser(user);
        goal.setUser(user);

        userProfileRepository.save(profile);
        dietPreferenceRepository.save(preference);
        healthGoalRepository.save(goal);

        return user;
    }

    /**
     * [核心功能]: 获取用户完整信息 (带缓存)
     * 使用 @Cacheable 自动缓存用户详情
     */
    @Cacheable(value = "userDetails", key = "#userId")
    public Optional<User> getUserDetails(Long userId) {
        System.out.println("正在从 MySQL 查询用户详情: " + userId);
        return userRepository.findByIdWithDetails(userId);
    }

    /**
     * [核心功能]: 更新用户信息 (并清除缓存)
     */
    @Transactional
    @CacheEvict(value = "userDetails", key = "#userId")
    public User updateUserDetails(Long userId, UserRegistrationData data) {
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新 UserProfile
        UserProfile profile = user.getUserProfile();
        profile.setHeight(data.getHeight());
        profile.setWeight(data.getWeight());
        profile.setActivityLevel(data.getActivityLevel());
        // ... 更新 BMR/TDEE (需要算法)
        userProfileRepository.save(profile);

        // 更新 DietPreference
        DietPreference preference = user.getDietPreference();
        preference.setAllergens(data.getAllergens());
        preference.setTaboos(data.getTaboos());
        preference.setTaste_preference(data.getTaste_preference());
        preference.setEating_habit(data.getEating_habit());
        preference.setEquipment_limit(data.getEquipment_limit());
        dietPreferenceRepository.save(preference);

        // 更新 HealthGoal
        HealthGoal goal = user.getHealthGoal();
        goal.setGoal_type(data.getGoalType());
        goal.setTarget_weight(data.getTarget_weight());
        goal.setTarget_date(data.getTarget_date());
        goal.setHeat_target(data.getHeat_target());
        healthGoalRepository.save(goal);

        System.out.println("已更新 MySQL 并清除用户详情缓存: " + userId);
        return user;
    }
}
