package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

//用于全文检索查询
public interface ListService {
    //根据传递的对象保存到kibana
    public void saveSkuInfo(SkuLsInfo skuLsInfo);
    //发送skuInfo信息集合
    public SkuLsInfo copySkuToList(SkuInfo skuInfo);
    //获取查询信息结果
    public SkuLsResult search(SkuLsParams skuLsParams);

}
