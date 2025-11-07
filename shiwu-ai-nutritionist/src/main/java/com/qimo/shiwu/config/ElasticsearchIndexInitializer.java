package com.qimo.shiwu.config;

import com.qimo.shiwu.data.es.ProductDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import jakarta.annotation.PostConstruct;

/**
 * ===================================================================
 * 【新功能】: Elasticsearch 索引初始化器
 * ===================================================================
 * [开发者注意]:
 * 这个组件会在 Spring Boot 启动时自动运行。
 * * 它的职责是检查 "products_shiwu" 索引是否存在。
 * 如果不存在，它会根据 ProductDocument.class 上的注解 (如 @Field)
 * 自动创建索引并配置映射 (Mapping)。
 * * 这确保了 DataLayerTestRunner 在运行搜索之前，索引是存在的。
 */
@Configuration
public class ElasticsearchIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexInitializer.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void createIndexIfNeeded() {
        // 获取与 ProductDocument 实体绑定的索引操作客户端
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);

        // 检查索引是否存在
        if (!indexOps.exists()) {
            log.info("Elasticsearch 索引 'products_shiwu' 不存在。正在创建...");
            try {
                // 1. 创建索引 (基于 @Document)
                indexOps.create();

                // 2. 应用映射 (基于 @Field)
                // 这一步至关重要，它会配置 "productName" 为 text, "tags" 为 keyword 等。
                indexOps.putMapping();

                log.info("索引 'products_shiwu' 创建并应用映射成功!");
            } catch (Exception e) {
                log.error("创建 ES 索引 'products_shiwu' 失败。", e);
            }
        } else {
            log.info("Elasticsearch 索引 'products_shiwu' 已存在，跳过创建。");
        }
    }
}
