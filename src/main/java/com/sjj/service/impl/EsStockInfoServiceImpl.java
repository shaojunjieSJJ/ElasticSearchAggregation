package com.sjj.service.impl;

import com.google.common.collect.Lists;
import com.sjj.dao.EsStockInfoRepository;
import com.sjj.entity.EsStockInfo;
import com.sjj.service.EsStockInfoService;
import lombok.extern.java.Log;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by 邵少 on 2019/10/12.
 */
@Log
@Service
@Transactional
public class EsStockInfoServiceImpl implements EsStockInfoService{

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private EsStockInfoRepository esStockInfoRepository;


    @Override
    public void listSaves() {
        // id, itemId, typeId, skuId, num, houseId
        List<EsStockInfo> esStockInfoList = Lists.newArrayList();
        esStockInfoList.add(new EsStockInfo(1L, 1L, 1L, "skuId001", 17, 1));
        esStockInfoList.add(new EsStockInfo(2L, 1L, 1L, "skuId002", 17, 1));

        esStockInfoList.add(new EsStockInfo(3L, 2L, 1L, "skuId003", 17, 1));
        esStockInfoList.add(new EsStockInfo(4L, 2L, 1L, "skuId001", 17, 2));

        esStockInfoList.add(new EsStockInfo(5L, 3L, 2L, "skuId002", 17, 2));
        esStockInfoList.add(new EsStockInfo(6L, 3L, 2L, "skuId003", 17, 2));

        esStockInfoList.add(new EsStockInfo(7L, 4L, 2L, "skuId004", 17, 2));
        esStockInfoList.add(new EsStockInfo(8L, 4L, 2L, "skuId001", 17, 4));

        esStockInfoList.add(new EsStockInfo(9L, 5L, 3L, "skuId002", 17, 4));
        esStockInfoList.add(new EsStockInfo(10L, 5L, 3L, "skuId003", 17, 4));

        esStockInfoRepository.saveAll(esStockInfoList);
    }

    @Override
    public void aggregationByTypeIdAndHouseId() {

        // 如果只对一个字段进行分组写一个就好
        TermsAggregationBuilder tb1 = AggregationBuilders.terms("group_typeId").field("typeId");//typeId 是分组字段名，group_typeId是查询结果的别名
        TermsAggregationBuilder tb2 = AggregationBuilders.terms("group_houseId").field("houseId");//houseId 是分组字段名，group_houseId是查询结果的别名
        SumAggregationBuilder sb = AggregationBuilders.sum("sum_num").field("num");//num是求和字段名称，sun_num是结果别名

        // 注意顺序,决定先通过谁分组
        tb1.subAggregation(sb);	// 通过typeId字段分组统计总数
        tb2.subAggregation(sb); // 通过houseId字段分组统计总数
        tb1.subAggregation(tb2); // 合并

        /**
         * 构建查询条件
         * ........
         * must 相当于sql中and
         * should 相当于sql中的and
         * 两个最好不要混用
         * mustNot 不查xxx
         * */
        /**BoolQueryBuilder bqb = QueryBuilders.boolQuery();
         bqb.mustNot(QueryBuilders.termQuery("houseId", 1));
         // 注意QueryBuilders.termsQuery和QueryBuilders.termQuery两个方法的不同
         List<Long> itemIds = Lists.newArrayList(1L,2L,3L);
         bqb.must(QueryBuilders.termsQuery("itemId", itemIds)); // 相当于sql中  item_id in(1,2,3)
         bqb.must(QueryBuilders.termQuery("skuId","skuId003"));
         // ............
         */

        // 指定索引和类型和聚合对象（tb1）
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//        		.withQuery(bqb)
                .withIndices("erp-stockinfo")
                .withTypes("stockInfo")
                .addAggregation(tb1)
                .build();
        // 执行语句获取聚合结果
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });
        // 获取聚合结果
        Terms term = aggregations.get("group_typeId");// 获取结果后进行解析
        if (term.getBuckets().size() > 0) {
            for (Bucket bk : term.getBuckets()) {
                int typeId = bk.getKeyAsNumber().intValue();
                log.info("typeId:{}" + typeId);
                // 得到所有子聚合
                Map<String, Aggregation> subaggmap = bk.getAggregations().asMap();
                log.info("subaggmap:{}" + subaggmap);
                double value = ((InternalSum)subaggmap.get("sum_num")).getValue();
                int num = Double.valueOf(value).intValue();
                log.info("num:{}" + num);
                System.out.println("-----------------------------------");
                // 获取结果后进行解析
                Terms term2 = (Terms) bk.getAggregations().get("group_houseId");
                for (Terms.Bucket bk2 : term2.getBuckets()) {
                    int houseId = bk2.getKeyAsNumber().intValue();
                    log.info("houseId:" + houseId);
                    //得到所有子聚合
                    Map<String, Aggregation> subaggmap2 = bk2.getAggregations().asMap();
                    double value2 = ((InternalSum)subaggmap2.get("sum_num")).getValue();
                    int num2 = Double.valueOf(value2).intValue();
                    log.info("num2:{}" + num2);
                }
                log.info("一次循环结束--------------------------");
            }
        }

    }

    @Override
    public void aggregationByTypeId() {

        TermsAggregationBuilder tb1 = AggregationBuilders.terms("group_typeId").field("typeId");//typeId 是分组字段名，group_typeId是查询结果的别名
        SumAggregationBuilder sb = AggregationBuilders.sum("sum_num").field("num");//num是求和字段名称，sun_num是结果别名

        tb1.subAggregation(sb);	// 通过typeId字段分组统计总数

        /**
         * 构建查询条件
         * ........
         * must 相当于sql中and
         * should 相当于sql中的and
         * 两个最好不要混用
         * mustNot 不查xxx
         * */
        /**BoolQueryBuilder bqb = QueryBuilders.boolQuery();
         bqb.mustNot(QueryBuilders.termQuery("houseId", 1));
         // 注意QueryBuilders.termsQuery和QueryBuilders.termQuery两个方法的不同
         List<Long> itemIds = Lists.newArrayList(1L,2L,3L);
         bqb.must(QueryBuilders.termsQuery("itemId", itemIds)); // 相当于sql中  item_id in(1,2,3)
         bqb.must(QueryBuilders.termQuery("skuId","skuId003"));
         // ............
         */

        // 指定索引和类型和聚合对象（tb1）
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//        		.withQuery(bqb)
                .withIndices("erp-stockinfo")
                .withTypes("stockInfo")
                .addAggregation(tb1)
                .build();
        // 执行语句获取聚合结果
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });
        // 获取聚合结果
        Terms term = aggregations.get("group_typeId");// 获取结果后进行解析
        if (term.getBuckets().size() > 0) {
            for (Bucket bk : term.getBuckets()) {
                int typeId = bk.getKeyAsNumber().intValue();
                log.info("typeId:{}" + typeId);
                // 得到所有子聚合
                Map<String, Aggregation> subaggmap = bk.getAggregations().asMap();
                log.info("subaggmap:{}" + subaggmap);
                double value = ((InternalSum)subaggmap.get("sum_num")).getValue();
                int num = Double.valueOf(value).intValue();
                log.info("num:{}" + num);
                log.info("一次循环结束--------------------------");
            }
        }
    }
}
