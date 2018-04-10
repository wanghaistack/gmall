package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    public List<UserInfo> getAllUserInfo();

    public List<UserInfo> getUserInfoList(UserInfo userInfo);

    public UserInfo getUserInfo(UserInfo userInfo);

    public void delete(UserInfo userInfo);

    public void addUserInfo(UserInfo userInfo);

    public void updateUserInfo(UserInfo userInfo);

    public List<UserAddress> getUserAddressList(String userId);
}
