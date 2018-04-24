package com.atguigu.gmall.cart.cookie;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.util.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CookieCartUtil {
    private String COOKIE_CART_NAME="CART";
    //cookie的过期时间
   private int COOKIE_TIME_OUT = 7 * 24 * 3600;

    public void addCookieCartInfo(HttpServletRequest request, HttpServletResponse response, SkuInfo skuInfo, int skuNum) {
        //设置cookieName
        List<CartInfo> cartInfoCookieList = new ArrayList<>();
        //先从cookie中获取值
        String cartInfoJsons = CookieUtil.getCookieValue(request, COOKIE_CART_NAME, true);
        boolean ifExist=false;
        if (cartInfoJsons != null && cartInfoJsons.length() > 0) {
            cartInfoCookieList = JSON.parseArray(cartInfoJsons, CartInfo.class);

            for (CartInfo cartInfo : cartInfoCookieList) {
                if (cartInfo.getSkuId().equals(skuInfo.getId())) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    ifExist=true;
                    break;
                }
            }
        }
        if (!ifExist){
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuInfo.getId());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoCookieList.add(cartInfo);

        }
        String cartInfoCookie = JSON.toJSONString(cartInfoCookieList);
        CookieUtil.setCookie(request,response,COOKIE_CART_NAME,cartInfoCookie,COOKIE_TIME_OUT,true);

    }
    public List<CartInfo> getCartInfoList(HttpServletRequest request){
        String cookieCartList = CookieUtil.getCookieValue(request, COOKIE_CART_NAME, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cookieCartList, CartInfo.class);
        return  cartInfoList;
    }
    public void deleteCookie(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,COOKIE_CART_NAME);
    }
}
