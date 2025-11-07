package com.qimo.shiwu.service;

import com.qimo.shiwu.data.es.ProductDocument;
import com.qimo.shiwu.data.es.ProductSearchRepository;
import com.qimo.shiwu.data.mysql.entity.SKU;
import com.qimo.shiwu.data.mysql.repository.SKURepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ===================================================================
 * 服务 5: 数据同步服务 (T+1 批量 + 近实时单条)
 * ===================================================================
 * [开发者注意]:
 * 负责保持 MySQL 和 Elasticsearch 的数据一致性。
 * (需要在主启动类上加 @EnableScheduling)
 */
@Service
public class DataSyncService {

    @Autowired
    private SKURepository skuRepository; // MySQL 仓库
    @Autowired
    private ProductSearchRepository productSearchRepository; // ES 仓库

    /**
     * T+1 批量同步任务
     * cron = "0 0 3 * * ?" 表示每天凌晨3点整执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(readOnly = true) // 同步任务是只读的，声明为只读事务可以优化性能
    public void syncAllProductsToES() {
        System.out.println("开始执行 [MySQL -> ES] T+1 批量同步任务...");
        List<SKU> skuList = skuRepository.findAllActiveSKUsForSync();
        if (skuList.isEmpty()) {
            System.out.println("同步任务结束：没有需要同步的数据。");
            return;
        }
        List<ProductDocument> documents = skuList.stream()
                .map(this::convertSkuToDocument)
                .collect(Collectors.toList());

        productSearchRepository.saveAll(documents);
        System.out.println("同步任务完成：成功同步 " + documents.size() + " 条商品数据到 ES。");
    }

    /**
     * [核心功能]: 近实时同步单个 SKU
     * 在订单服务或后台更新商品后调用，立即更新 ES。
     * @param skuId
     */
    public void syncSingleSku(Long skuId) {
        System.out.println("执行 [MySQL -> ES] 单条近实时同步: " + skuId);
        // 使用新查询来获取单个SKU的完整信息
        Optional<SKU> skuOpt = skuRepository.findActiveSkuByIdForSync(skuId);

        if (skuOpt.isPresent()) {
            ProductDocument doc = convertSkuToDocument(skuOpt.get());
            productSearchRepository.save(doc); // 更新或插入
            System.out.println("单条同步成功: " + skuId);
        } else {
            // SKU 可能被删除了或状态变为 false
            productSearchRepository.deleteById(skuId.toString());
            System.out.println("单条同步删除: " + skuId);
        }
    }

    /**
     * 辅助方法：将 SKU 实体转换为 ES 文档
     */
    private ProductDocument convertSkuToDocument(SKU sku) {
        ProductDocument doc = new ProductDocument();
        doc.setId(sku.getSkuId().toString()); // ES ID
        doc.setSkuId(sku.getSkuId());
        doc.setProductId(sku.getProduct().getProductId());
        doc.setProductName(sku.getProduct().getProductName());
        doc.setProductDesc(sku.getProduct().getProductDesc());
        doc.setSkuSize(sku.getSkuSize());
        doc.setSkuPrice(sku.getSkuPrice());
        doc.setInventoryQuantity(sku.getInventory().getInventoryQuantity());

        // [核心逻辑] TODO: 实现一个更健壮的标签生成器
        // 这里的示例逻辑非常简单
        List<String> tags = new ArrayList<>();
        if (sku.getProduct().getProductName().contains("鸡胸")) {
            tags.add("高蛋白");
            tags.add("低脂肪");
        }
        if (sku.getProduct().getProductName().contains("糙米")) {
            tags.add("高纤维");
        }
        if (sku.getSkuHeat().doubleValue() < 200) {
            tags.add("低卡");
        }
        doc.setTags(tags);

        return doc;
    }
}
