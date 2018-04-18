package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;

//用于全文检索查询
public interface ListService {
    //根据传递的对象保存到kibana
    public void saveSkuInfo(SkuLsInfo skuLsInfo);
    //发送skuInfo信息集合
    public SkuLsInfo copySkuToList(SkuInfo skuInfo);

}
