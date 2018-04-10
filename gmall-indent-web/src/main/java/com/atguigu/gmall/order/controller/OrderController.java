package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {
    @Reference
    UserService userService;

    @ResponseBody
    @RequestMapping( "/order/{userId}")
    public List<UserAddress> initOrder(@PathVariable("userId")String userId){
        UserAddress userAddress=new UserAddress();
        userAddress.setUserId(userId);
        JSON.toJSONString("");
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        return userAddressList;
    }

}
