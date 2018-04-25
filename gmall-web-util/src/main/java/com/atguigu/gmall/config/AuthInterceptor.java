package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constutil.HttpclientUtil;
import com.atguigu.gmall.constutil.WebConst;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.LoginRequire;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String newToken = request.getParameter("newToken");
        if (newToken != null && newToken.length() > 0) {
            CookieUtil.setCookie(request, response, "token", newToken, WebConst.COOKIE_TIME_OUT, false);
        }
        if (newToken == null) {
            newToken = CookieUtil.getCookieValue(request, "token", false);
        }
        if (newToken != null) {
            Map map=getMap(newToken);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }
        //检查是否需要验证用户已经登录
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation != null) {
            //获取当前登录ip
            String currentIp = request.getHeader("x-forwarded-for");
            if (newToken != null) {
                String result = HttpclientUtil.doGet(WebConst.VERIFY_URL + "?token=" + newToken + "&currentIp=" + currentIp);
                if ("success".equals(result)) {
                    Map map=getMap(newToken);
                    String userId = (String) map.get("userId");
                    request.setAttribute("userId", userId);
                    return true;
                } else {
                    if (methodAnnotation.autoRedirect()) {
                        String requestUrl = request.getRequestURL().toString();
                        String encode = URLEncoder.encode(requestUrl, "UTF-8");
                        response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encode);
                        return false;
                    }
                }
            }


        }

        return true;
    }
    public Map getMap(String newToken){
        //读取token
        String tokenUserInfo = StringUtils.substringBetween(newToken, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tookenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tookenJson = null;
        try {
            tookenJson = new String(tookenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tookenJson, Map.class);
        return map;
    }
}
