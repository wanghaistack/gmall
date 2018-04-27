package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

public interface CartService {
    void addCartInfoList(CartInfo cartInfo, String userId,SkuInfo skuInfo);

    List<CartInfo> getCartInfoList(String userId);

    List<CartInfo> mergeCartInfoList(List<CartInfo> cartInfoFromCookie, String userId);

    void checkCart(String skuId, String isChecked, String userId);

    public List<CartInfo> loadCartCache(String userId);

    //删除用户购物车信息
    public void deleteCheckedCartInfo(String userId,List<String>skuIds);
}
