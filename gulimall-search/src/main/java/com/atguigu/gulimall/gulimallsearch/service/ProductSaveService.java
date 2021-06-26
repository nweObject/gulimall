package com.atguigu.gulimall.gulimallsearch.service;

import com.atguigu.common.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author zy
 * @creat 2021-06-{DAY}-{TIME}
 */
public interface ProductSaveService {

    /**
     * 上架商品更新es数据
     * */
    Boolean productStatusUp(List<SkuEsModel> esModels) throws IOException;
}
