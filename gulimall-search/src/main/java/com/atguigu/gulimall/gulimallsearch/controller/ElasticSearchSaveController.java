package com.atguigu.gulimall.gulimallsearch.controller;

import com.atguigu.common.es.SkuEsModel;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author zy
 * @creat 2021-06-{DAY}-{TIME}
 */
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSearchSaveController {

    @Autowired
    ProductSaveService productSaveService;

    /**
     *商品上架
     */
    @RequestMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> esModels) {
        Boolean result = false;
        try {
           result = productSaveService.productStatusUp(esModels);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("商品上架错误{}",e);
            R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (result) {
            return R.ok();
        }else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
