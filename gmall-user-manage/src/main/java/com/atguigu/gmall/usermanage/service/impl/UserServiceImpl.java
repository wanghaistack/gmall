package com.atguigu.gmall.usermanage.service.impl;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@com.alibaba.dubbo.config.annotation.Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> getAllUserInfo() {
        List<UserInfo> userInfos = userInfoMapper.selectAll();
         System.out.println(userInfos);
         return userInfos;

    }

    @Override
    public List<UserInfo> getUserInfoList(UserInfo userInfo) {
        List<UserInfo> userInfos=null;
        Example example=new Example(UserInfo.class);
        example.createCriteria().andLike("loginName","%"+userInfo.getLoginName()+"%");
        userInfos=userInfoMapper.selectByExample(example);
        return userInfos;
    }

    @Override
    public UserInfo getUserInfo(UserInfo userInfo) {
        UserInfo userInfo1=null;
        userInfo1 = userInfoMapper.selectOne(userInfo);
        return userInfo1;
    }

    @Override
    public void delete(UserInfo userInfo) {
        userInfoMapper.deleteByPrimaryKey(userInfo.getId());

        Example example=new Example(UserInfo.class);
        example.createCriteria().andLike("name","%"+userInfo.getName()+"%");
        userInfoMapper.deleteByExample(example);

    }

    @Override
    public void addUserInfo(UserInfo userInfo) {
        //会覆盖数据默认值
        //userInfoMapper.insert(userInfo);
        //不会覆盖数据默认值
        userInfoMapper.insertSelective(userInfo);

    }

    @Override
    public void updateUserInfo(UserInfo userInfo) {
    Example example=new Example(UserInfo.class);
    example.createCriteria().andLike("loginName","%"+userInfo.getLoginName()+"%");
    userInfo.setLoginName(null);
    userInfoMapper.updateByExampleSelective(userInfo,example);

    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress=new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> list = userAddressMapper.select(userAddress);
        return list;
    }
}
