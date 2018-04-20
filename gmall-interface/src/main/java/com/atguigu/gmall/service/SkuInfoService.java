package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface SkuInfoService {

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);


    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    public SkuInfo getSkuInfoDB(String skuId);

    List<SkuInfo> getSkuInfoList(String spuId);

    void deleteSkuInfoBySkuId(String skuId);
}
