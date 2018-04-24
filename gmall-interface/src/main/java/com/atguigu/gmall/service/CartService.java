package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

public interface CartService {
    void addCartInfoList(CartInfo cartInfo, String userId,SkuInfo skuInfo);
}
