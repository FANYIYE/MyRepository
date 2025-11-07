package com.qimo.shiwu.data.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.List;

/**
 *  Elasticsearch 搜索层
 * [开发者注意]:
 * ES 的核心是 "反规范化"，`ProductDocument` 就是这一思想的体现。
 * 我们把 MySQL 中多张表的数据聚合到这里，是为了实现最快的搜索速度。
 * 搜索时只查 ES，获取 ID 后再回查 MySQL/Redis 获取最新详情。
 */

/**
 * ES 文档实体 (Document)
 * 这是一个反规范化的模型，聚合了 Product, SKU, Inventory 的信息。
 * 专门用于搜索。
 */
@Data
@Document(indexName = "products_shiwu")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long skuId;

    @Field(type = FieldType.Long)
    private Long productId;

    @Field(type = FieldType.Text)
    private String productName;

    @Field(type = FieldType.Text)
    private String productDesc;

    @Field(type = FieldType.Keyword)
    private String skuSize;

    @Field(type = FieldType.Double)
    private BigDecimal skuPrice;

    @Field(type = FieldType.Integer)
    private Integer inventoryQuantity;

    @Field(type = FieldType.Keyword)
    private List<String> tags;
}
