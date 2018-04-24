package com.atguigu.gmall.cart;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.CookieUtil;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CookieCartUtil {
    @Reference
    SkuInfoService skuInfoService;
    //cookie前缀
    public static final String CART_COOKIE_PREFIX = "cart:";
    //cookie后缀
    public static final String CART_COOKIE_SUFFIX = ":cookie";
    //cookie的过期时间
    public static final int COOKIE_TIME_OUT = 7 * 24 * 3600;

    public void addCookieCartInfo(HttpServletRequest request, HttpServletResponse response, String skuId, int skuNum) {
        //设置cookieName
        String cookieName = CART_COOKIE_PREFIX + skuId + CART_COOKIE_SUFFIX;
        //根据skuId获取skuInfo信息
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        List<CartInfo> cartInfoCookieList = new ArrayList<>();
        //先从cookie中获取值
        String cartInfoJsons = CookieUtil.getCookieValue(request, cookieName, true);
        if (cartInfoJsons != null && cartInfoJsons.length() > 0) {
            List<CartInfo> cartInfoList = JSON.parseArray(cartInfoJsons, CartInfo.class);

            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuInfo.getId())) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    cartInfoCookieList.add(cartInfo);
                }
            }
            String cartInfoCookie = JSON.toJSONString(cartInfoCookieList);
            CookieUtil.setCookie(request, response, cookieName, cartInfoCookie, COOKIE_TIME_OUT, true);
        } else {

            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuInfo.getId());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoCookieList.add(cartInfo);
            String cartInfoCookie = JSON.toJSONString(cartInfoCookieList);
            CookieUtil.setCookie(request, response, cookieName, cartInfoCookie, COOKIE_TIME_OUT, true);
        }


    }
}
