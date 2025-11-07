package com.qimo.shiwu.service;
import com.qimo.shiwu.data.es.ProductDocument;
import com.qimo.shiwu.data.es.ProductSearchRepository;
import com.qimo.shiwu.data.mysql.entity.SKU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ===================================================================
 * 服务 3: 商品搜索服务 (封装 ES 召回 + 精排)
 * ===================================================================
 * [开发者注意]:
 * 封装《开题报告》中的“召回 + 精排”完整流程。
 */
@Service
public class ProductSearchService {

    @Autowired
    private ProductSearchRepository productSearchRepository; // ES 仓库
    @Autowired
    private ProductCacheService productCacheService; // 缓存/MySQL 服务

    /**
     * [核心功能]: 封装“召回 + 精排”的完整搜索流程
     * 后续开发者调用此方法即可获取最终排序的商品列表。
     *
     * @param query 搜索词 (例如来自 AI 食谱的 "杭椒")
     * @param tags 标签过滤列表 (例如 ["高蛋白", "低脂"])
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param pageable 分页和排序对象
     * @return 排序后的 SKU 分页结果
     */
    public Page<SKU> searchProducts(String query, List<String> tags,
                                    BigDecimal minPrice, BigDecimal maxPrice,
                                    Pageable pageable) {

        // 1. [召回 Recall] - 从 Elasticsearch 召回相关文档
        System.out.println("ES 召回: " + query);

        // 如果没有提供价格范围，设置默认值
        BigDecimal effectiveMinPrice = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        BigDecimal effectiveMaxPrice = (maxPrice != null) ? maxPrice : new BigDecimal("999999");
        List<String> effectiveTags = (tags != null) ? tags : new ArrayList<>();

        Page<ProductDocument> documents = productSearchRepository.searchProducts(
                query,
                effectiveTags,
                effectiveTags.size(), // 传入标签数量，用于 "minimum_should_match"
                effectiveMinPrice,
                effectiveMaxPrice,
                pageable
        );

        if (!documents.hasContent()) {
            return Page.empty(pageable);
        }

        // 2. [精排 Rank] - 获取最新数据 (Data Hydration)
        // 从召回的文档中提取 skuIds
        List<Long> skuIds = documents.getContent().stream()
                .map(ProductDocument::getSkuId)
                .toList();

        // 批量从缓存(Redis)或数据库(MySQL)获取最新的 SKU 详情
        // (这保证了价格和库存的实时性)
        List<SKU> rankedSkus = skuIds.stream()
                .map(id -> productCacheService.getSkuById(id).orElse(null))
                .filter(Objects::nonNull) // 过滤掉可能已下架的商品
                .collect(Collectors.toList());

        // 3. [精排 Rank] - 执行业务排序算法
        // TODO: 在这里实现《开题报告》中提到的复杂排序算法
        // 例如: Jaro-Winkler 距离, 加权评分(好评率, 销量等)
        // (目前暂时返回按 ES 召回顺序排序的结果)

        // 重新组装 Page 对象
        return new PageImpl<>(rankedSkus, pageable, documents.getTotalElements());
    }
}
