package com.atguigu.gulimall.gulimallsearch;


import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Resource
    RestHighLevelClient client;

    @Data
    class User {
        private String name;
        private int age;
        private String address;
    }


    @Test
    public void contextLoads() {

        System.out.println(client);

    }

    @Test
    public void indexUsers() throws IOException {
        IndexRequest request = new IndexRequest("users");
        User user = new User();
        user.setAddress("重庆");
        user.setName("张三");
        user.setAge(18);
        String json = JSON.toJSONString(user);
        request.source(json, XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        RestStatus status = response.status();
        System.out.println(status);
    }

    @Test
    public void getUser() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder builder = getBuilder();
        searchRequest.source(builder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = search.getHits();
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
        }
    }

    public SearchSourceBuilder getBuilder() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "张三"));
        return searchSourceBuilder;
    }
}
