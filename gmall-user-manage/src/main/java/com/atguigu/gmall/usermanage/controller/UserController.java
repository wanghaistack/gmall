package com.atguigu.gmall.usermanage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("/user")
    public ResponseEntity<List<UserInfo>> getAllUserInfo() {
        List<UserInfo> allUserInfo = userService.getAllUserInfo();
        return ResponseEntity.ok(allUserInfo);
    }

    @RequestMapping("hello")
    public String sayHello() {
        return "HelloWorld";
    }

    @RequestMapping("/user/{loginName}")
    public UserInfo getUserInfo(@PathVariable("loginName") String loginName) {
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginName(loginName);
        userInfo = userService.getUserInfo(userInfo);
        return userInfo;
    }

    @RequestMapping("/users")
    public void updateUserInfo(UserInfo userInfo) {
        //UserInfo userInfo=null;
        //userInfo.setName(name);
        //  userInfo.setLoginName(loginName);
        userService.updateUserInfo(userInfo);

    }

    @RequestMapping("/userid/{id}")
    public void deleteUserInfo(UserInfo userInfo, @PathVariable("id") String id) {
        userInfo.setId(id);
        userService.delete(userInfo);
    }
    @RequestMapping("/useraddress/{userId}")
    public ResponseEntity<List<UserAddress>> getUserAddressList(@PathVariable("userId")String userId){
        List<UserAddress> addressList = userService.getUserAddressList(userId);
        return ResponseEntity.ok(addressList);
    }

}
