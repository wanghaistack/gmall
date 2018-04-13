package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;

import java.util.List;

public interface SpuInfoService {
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    List<BaseSaleAttr> getBaseSaleAttrrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuInfo> getSpuList(String catalog3Id);

    List<SpuImage> getSpuImgList(String spuId);

    BaseAttrInfo getAttrInfoList(String ctg3Id);

}
