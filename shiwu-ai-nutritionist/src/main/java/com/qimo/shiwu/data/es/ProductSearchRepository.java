package com.qimo.shiwu.data.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * [核心功能]: 复杂的多条件布尔查询 (Bool Query)
     * 这是《开题报告》中“召回”阶段的核心。
     * * @param query 用户的搜索词 (e.g., "鸡胸肉")
     * @param tags 标签列表 (e.g., ["高蛋白", "低脂"])
     * @param tagCount 标签数量 (用于 "minimum_should_match")
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param pageable 分页对象
     * @return
     */
    @Query("""
    {
      "bool": {
        "must": [
          {
            "multi_match": {
              "query": "?0",
              "fields": ["productName", "productDesc"]
            }
          }
        ],
        "filter": [
          {
            "bool": {
              "must": [
                {
                  "terms": {
                    "tags": {
                      "value": "?1",
                      "minimum_should_match": "?2"
                    }
                  }
                },
                {
                  "range": {
                    "skuPrice": {
                      "gte": "?3",
                      "lte": "?4"
                    }
                  }
                }
              ]
            }
          }
        ]
      }
    }
    """)
    Page<ProductDocument> searchProducts(String query, List<String> tags, int tagCount,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         Pageable pageable);

}
