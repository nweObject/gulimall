package com.atguigu.gulimall.product.feign;

import com.atguigu.common.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author zy
 * @creat 2021-06-{DAY}-{TIME}
 */
@FeignClient(name = "gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    public R productStatusUp(List<SkuEsModel> esModels);
}
