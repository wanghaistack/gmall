package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.cookie.CookieCartUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartCotroller {
    @Reference
    CartService cartService;
    @Reference
    SkuInfoService skuInfoService;
    @Autowired
    CookieCartUtil cookieCartUtil;
   //自定义注解，加上之后被拦截器拦下，不需要转发到登录页面，但必须得走拦截器
    @LoginRequire(autoRedirect = false)
    //从item页面中发起的请求路径
    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    public String addToCart(CartInfo cartInfo, HttpServletRequest request, HttpServletResponse response){
        SkuInfo skuInfo = skuInfoService.getSkuInfo(cartInfo.getSkuId());
        if (skuInfo!=null){
            request.setAttribute("skuInfo",skuInfo);
        }
        //从拦截器中放在request域中获取的userid
        String userId = (String) request.getAttribute("userId");

        //如果userId不为空则说明用户已登录，则存放到数据库中并存放到redis缓存中
        if (userId!=null){
           cartService.addCartInfoList(cartInfo,userId,skuInfo);
        }else {
            //如果为空，则把cartInfo放入到cookie中
            cookieCartUtil.addCookieCartInfo(request,response,skuInfo,cartInfo.getSkuNum());
        }

        request.setAttribute("skuNum",cartInfo.getSkuNum());
        return "success";
    }
    @RequestMapping("cartList")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public String toCartList(HttpServletRequest request,HttpServletResponse response){
        String userId = request.getParameter("userId");
        List<CartInfo> cartInfoList=new ArrayList<>();
         List<CartInfo> cartInfoFromCookie= cookieCartUtil.getCartInfoList(request);
        //如果用户已登录
        if (userId!=null){
            if (cartInfoFromCookie!=null && cartInfoFromCookie.size()>0){
                //与前台合并
                cartInfoList= cartService.mergeCartInfoList(cartInfoFromCookie,userId);
                //删除cookie的值
                cookieCartUtil.deleteCookie(request,response);
            }else {
                cartInfoList = cartService.getCartInfoList(userId);
            }
            request.setAttribute("cartInfoList",cartInfoList);

        }else {
            //用户未登录
            cartInfoList = cookieCartUtil.getCartInfoList(request);
            request.setAttribute("cartInfoList",cartInfoList);

        }
        return "cartList";

    }
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId=(String) request.getAttribute("userId");
        if (userId!=null){
            cartService.checkCart(skuId,isChecked,userId);
        }
        return ;
    }
}
