package com.qimo.shiwu.runner;
import com.qimo.shiwu.dto.UserRegistrationData;
import com.qimo.shiwu.data.mysql.entity.User;
import com.qimo.shiwu.data.mysql.repository.UserRepository;
import com.qimo.shiwu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 数据层启动测试器
 *
 * [开发者注意]:
 * 这是一个 `@CommandLineRunner`，它会在 Spring Boot 应用**启动完成**后自动执行。
 * 它会验证 `UserService` 的复杂注册流程和缓存是否按预期工作。
 * * 在生产环境中可以禁用或移除此类。
 */
@Component
public class DataLayerTestRunner implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository; // (用于验证)

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=============================================");
        System.out.println("========== “食悟” 数据层测试程序启动 ==========");
        System.out.println("=============================================");

        String testUsername = "testRunnerUser";

        try {
            // 1. 构造测试数据
            UserRegistrationData testData = createTestData(testUsername);
            User registeredUser = null;

            // 2. 尝试注册或获取用户
            System.out.println("[测试] 正在尝试注册或获取用户: " + testUsername);
            Optional<User> existingUserOpt = userRepository.findByUserName(testUsername);

            if (existingUserOpt.isEmpty()) {
                // 用户不存在，执行注册
                try {
                    registeredUser = userService.registerNewUser(testData);
                    System.out.println("[成功] 新用户注册成功! 用户 ID: " + registeredUser.getUserId());
                } catch (Exception e) {
                    System.err.println("[失败] 注册时发生意外错误: " + e.getMessage());
                    throw e; // 抛出异常
                }
            } else {
                // 用户已存在
                registeredUser = existingUserOpt.get();
                System.out.println("[跳过] 用户 " + testUsername + " 已存在 (ID: " + registeredUser.getUserId() + "). 跳过注册。");
            }

            // 3. 验证注册/获取结果 (使用刚注册或获取到的用户)
            if (registeredUser == null) {
                System.err.println("[失败] 验证失败! 无法获取到测试用户!");
                return;
            }

            System.out.println("[测试] 正在验证用户数据 (ID: " + registeredUser.getUserId() + ")...");
            // 第一次获取 (应该走 MySQL, 打印 "正在从 MySQL 查询...")
            Optional<User> userOpt1 = userService.getUserDetails(registeredUser.getUserId());

            if (userOpt1.isPresent()) {
                User user = userOpt1.get();
                System.out.println("[成功] 验证成功 (来自 MySQL)! 成功获取到用户: " + user.getUserName());
                System.out.println("       - 健康目标: " + user.getHealthGoal().getGoal_type());
                System.out.println("       - 过敏源: " + user.getDietPreference().getAllergens());

                // 4. 验证缓存
                System.out.println("[测试] 正在验证 Redis 缓存...");
                // 第二次获取 (不应打印 "正在从 MySQL 查询...")
                Optional<User> userOpt2 = userService.getUserDetails(registeredUser.getUserId());

                if (userOpt2.isPresent()) {
                    System.out.println("[成功] 缓存验证成功! (如果上一行没有打印 '...查询...'，则代表 Redis 缓存命中)");
                } else {
                    System.err.println("[失败] 缓存验证失败! 第二次查询未获取到用户!");
                }

            } else {
                System.err.println("[失败] 验证失败! 未找到 ID 为 " + registeredUser.getUserId() + " 的用户!");
            }

        } catch (Exception e) {
            System.err.println("[严重错误] 数据层测试失败: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========== “食悟” 数据层测试程序结束 ==========");
    }

    /**
     * 辅助方法：创建一组固定的测试数据
     */
    private UserRegistrationData createTestData(String username) {
        UserRegistrationData data = new UserRegistrationData();
        data.setUsername(username);
        data.setPassword("runner_pass_123");
        data.setHeight(new BigDecimal("175.0"));
        data.setWeight(new BigDecimal("70.0"));
        data.setActivityLevel("中度");
        data.setAllergens("花生");
        data.setTaboos("香菜");
        data.setTaste_preference("清淡");
        data.setEating_habit("低卡");
        data.setEquipment_limit("无烤箱");
        data.setGoalType("减脂");
        data.setTarget_weight(new BigDecimal("65.0"));
        data.setTarget_date(LocalDateTime.now().plusMonths(3));
        data.setHeat_target(new BigDecimal("1800.0"));
        return data;
    }
}
