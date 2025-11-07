package com.qimo.shiwu.service;

import com.qimo.shiwu.data.mysql.entity.Inventory;
import com.qimo.shiwu.data.mysql.entity.Order;
import com.qimo.shiwu.data.mysql.entity.User;
import com.qimo.shiwu.data.mysql.repository.InventoryRepository;
import com.qimo.shiwu.data.mysql.repository.OrderRepository;
import com.qimo.shiwu.data.mysql.repository.UserRepository;
import com.qimo.shiwu.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ===================================================================
 * 服务 4: 订单服务 (封装事务 + 分布式锁)
 * ===================================================================
 * [开发者注意]:
 * 封装高并发下的订单创建(防超卖)逻辑。
 */
@Service
public class OrderService {

    @Autowired private StringRedisTemplate redisTemplate; // 用于分布式锁
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private SnowflakeIdGenerator idGenerator;
    @Autowired private ProductCacheService productCacheService;
    @Autowired private DataSyncService dataSyncService;
    @Autowired private UserRepository userRepository; // 假设需要关联用户

    private static final String LOCK_KEY_PREFIX = "lock:inventory:";

    /**
     * [核心功能]: 创建订单 (防超卖)
     * 封装了 Redis 分布式锁 和 MySQL 事务。
     */
    @Transactional
    public Order createOrder(Long userId, Long skuId, int quantityToBuy, String address) {
        // 根据 qimo.sql, inventory_id 和 sku_id 是一对一关系
        // 假设 inventory_id == skuId (如果不是, 你需要先查询 SKU 找到 inventory_id)
        Long inventoryId = skuId;
        String lockKey = LOCK_KEY_PREFIX + inventoryId;
        boolean hasLock = false;

        try {
            // 1. 获取 Redis 分布式锁 (简单实现)
            // 尝试获取锁，锁自动过期30秒
            hasLock = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 30, TimeUnit.SECONDS));

            if (!hasLock) {
                // 未获取到锁，系统繁忙
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            // 2. [事务内] 检查并扣减库存
            Inventory inventory = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));

            if (inventory.getInventoryQuantity() < quantityToBuy) {
                throw new RuntimeException("库存不足");
            }

            inventory.setInventoryQuantity(inventory.getInventoryQuantity() - quantityToBuy);
            inventory.setInventoryUpdateTime(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // 3. [事务内] 创建订单
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Order order = new Order();
            order.setOrderId(idGenerator.nextId());
            order.setOrderTime(LocalDateTime.now());
            order.setOrderAdress(address);

            // TODO: 在 Order 实体中添加 user 字段的 @ManyToOne 关联
            // order.setUser(user);

            // TODO: 处理订单与食谱的关联 (RecipeRecord)
            // order.setRecipeRecords(...);

            orderRepository.save(order);

            // 4. [事务提交后] 清理缓存 和 同步ES
            // Spring 的 @Transactional 会在方法成功返回后提交事务
            // (更优的实现是使用 TransactionalEventListener)

            productCacheService.evictSkuCache(skuId);
            dataSyncService.syncSingleSku(skuId); // 触发ES近实时同步

            return order;

        } catch (Exception e) {
            // 确保事务回滚
            throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
        } finally {
            // 5. 释放锁
            if (hasLock) {
                // 释放锁 (Lua 脚本保证原子性，防止释放他人锁)
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(
                        new DefaultRedisScript<>(script, Long.class),
                        List.of(lockKey),
                        "locked"
                );
            }
        }
    }
}
