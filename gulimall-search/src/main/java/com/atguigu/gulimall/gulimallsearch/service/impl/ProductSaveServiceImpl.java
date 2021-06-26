package com.atguigu.gulimall.gulimallsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.es.SkuEsModel;
import com.atguigu.gulimall.gulimallsearch.configs.GulimallElasticsearchConfig;
import com.atguigu.gulimall.gulimallsearch.constant.EsConstant;
import com.atguigu.gulimall.gulimallsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zy
 * @creat 2021-06-{DAY}-{TIME}
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient esClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> esModels) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        esModels.forEach(esModel -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(esModel.getSkuId().toString())
                    .source(JSON.toJSONString(esModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        BulkResponse bulk = esClient.bulk(bulkRequest, GulimallElasticsearchConfig.COMMON_OPTIONS);
        boolean result = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        if (!result) {
            log.info("上架了商品:{}", collect);
        }
        return !result;
    }
}
