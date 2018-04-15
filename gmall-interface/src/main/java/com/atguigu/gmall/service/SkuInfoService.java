package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface SkuInfoService {

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
