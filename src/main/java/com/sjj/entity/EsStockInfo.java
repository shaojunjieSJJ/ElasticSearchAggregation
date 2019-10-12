package com.sjj.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * Created by 邵少 on 2019/10/12.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "erp-stockinfo", type="stockInfo",shards = 3, replicas = 0)
public class EsStockInfo {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long itemId;// 商品id

    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String itemName;// 商品名称

    @Field(type = FieldType.Long)
    private Long typeId;// 商品类型id

    @Field(type = FieldType.Keyword)
    private String typeName;// 商品类型名称

    @Field(type = FieldType.Keyword)
    private String skuId;// skuId

    @Field(type = FieldType.Text, index = false)
    private String skuInfo;// sku信息

    @Field(type = FieldType.Integer)
    private Integer num;// 总数

    @Field(type = FieldType.Integer)
    private Integer houseId;// 仓库id

    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String houseName;// 仓库名称

    @Field(type = FieldType.Long)
    private Long brandId;// 品牌id

    @Field(type = FieldType.Text, analyzer="ik_smart", searchAnalyzer="ik_smart")
    private String brandName;// 品牌名称

    @Field(type = FieldType.Date)
    private Date created;// 创建时间

    @Field(type = FieldType.Date)
    private Date updated;// 修改时间

    public EsStockInfo(Long id, Long itemId, Long typeId, String skuId, Integer num, Integer houseId) {
        this.id = id;
        this.itemId = itemId;
        this.typeId = typeId;
        this.skuId = skuId;
        this.num = num;
        this.houseId = houseId;
    }

}
