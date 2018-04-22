package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constutil.WebConst;
import com.atguigu.gmall.util.CookieUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String newToken= request.getParameter("newToken");
        if (newToken!=null && newToken.length()>0){
            CookieUtil.setCookie(request,response,"token",newToken, WebConst.COOKIE_TIME_OUT,false);
        }
        if (newToken==null){
            newToken=CookieUtil.getCookieValue(request,"token",false);
        }
        if (newToken!=null){
            //读取token
            String tokenUserInfo = StringUtils.substringBetween(newToken, ",");
            Base64UrlCodec base64UrlCodec=new Base64UrlCodec();
            byte[] tookenBytes = base64UrlCodec.decode(tokenUserInfo);
            String tookenJson = new String(tookenBytes, "UTF-8");
            Map map = JSON.parseObject(tookenJson, Map.class);
            String nickName =(String) map.get("nickName");
            request.setAttribute("nickName",nickName);

        }

        return true;
    }
}
