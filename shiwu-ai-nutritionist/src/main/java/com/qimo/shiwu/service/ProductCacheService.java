package com.qimo.shiwu.service;
import com.qimo.shiwu.data.mysql.entity.SKU;
import com.qimo.shiwu.data.mysql.repository.SKURepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ===================================================================
 * 服务 2: 商品缓存服务
 * ===================================================================
 * [开发者注意]:
 * 封装 SKU 实体的 Cache-Aside (旁路缓存) 逻辑。
 */
@Service
public class ProductCacheService {

    @Autowired
    private SKURepository skuRepository; // MySQL 仓库

    /**
     * @Cacheable: 自动使用 Redis 缓存 (key = "skuDetails::[skuId]")
     */
    @Cacheable(value = "skuDetails", key = "#skuId")
    public Optional<SKU> getSkuById(Long skuId) {
        System.out.println("正在从 MySQL 查询 SKU: " + skuId);
        return skuRepository.findById(skuId);
    }

    /**
     * @CacheEvict: 清除缓存 (用于更新或删除后)
     */
    @CacheEvict(value = "skuDetails", key = "#skuId")
    public void evictSkuCache(Long skuId) {
        // 这个方法体可以是空的，注解会自动工作
        System.out.println("正在清除 SKU 缓存: " + skuId);
    }
}
