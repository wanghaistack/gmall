package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.JwtUtil;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassPortController {
    @Reference
    UserService userService;
    @Value("${token.key}")
    String TOKEN_KEY;
    @RequestMapping("index")
    public String toIndex(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";

    }
    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody
   public String login(HttpServletRequest request){
        //获取登录的ip地址， 在nginx配置
        String remoteAddr = request.getHeader("x-forwarded-for");
        //获取用户账号
        String loginName = request.getParameter("loginName");
        //获取用户登录的密码
        String passwd = request.getParameter("passwd");
        //设置用户信息，调用业务层处理业务
        //判断用户是否输入信息
        if (loginName!=null && passwd!=null){
            UserInfo userInfo=new UserInfo();
            userInfo.setLoginName(loginName);
            userInfo.setPasswd(passwd);
            UserInfo userInfoLogin = userService.login(userInfo);
            if (userInfoLogin==null){
                return "fail";
            }else {
                //调用JWT(java web util)工具类生成token
                //一般以ip地址为盐值，用户别名和用户id为用户信息,用的Base64编码可以解析不应放敏感数据。
                //生成新的token
                //key再配置文件中可以自定义配置
                    String nickName = userInfoLogin.getNickName();
                    String userInfoId = userInfoLogin.getId();
                    Map<String,Object> map=new HashMap<String,Object>();
                    map.put("nickName",nickName);
                    map.put("userInfoId",userInfoId);
                    String token = JwtUtil.encode(TOKEN_KEY, map, remoteAddr);
                    return token;

            }
        }


        return "fail";



    }
}
