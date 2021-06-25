package com.atguigu.common.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zy
 * @creat 2021-06-{DAY}-{TIME}
 */
@Data
public class SkuEsModel {
    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImage;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;

    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs {

        private String attrId;

        private String attrName;

        private String attrValue;
    }
}
