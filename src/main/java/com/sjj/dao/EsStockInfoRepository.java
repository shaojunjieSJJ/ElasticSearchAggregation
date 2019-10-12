package com.sjj.dao;

import com.sjj.entity.EsStockInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Created by 邵少 on 2019/10/12.
 */
public interface EsStockInfoRepository extends ElasticsearchRepository<EsStockInfo, Long> {


}
