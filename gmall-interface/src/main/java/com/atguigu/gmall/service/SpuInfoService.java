package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface SpuInfoService {
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    List<BaseSaleAttr> getBaseSaleAttrrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuInfo> getSpuList(String catalog3Id);

    List<SpuImage> getSpuImgList(String spuId);

    List<BaseAttrInfo> getAttrInfoList(String ctg3Id);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);


}
