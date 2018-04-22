package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.constutil.UserConst;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@com.alibaba.dubbo.config.annotation.Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;

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
        String passwd = userInfo.getPasswd();
        //md5加密设置密码
        String md5Hex = DigestUtils.md5Hex(passwd);
        userInfo.setPasswd(md5Hex);
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

    @Override
    public UserInfo login(UserInfo userInfo) {
       //获取用户登录信息，设置密码md5加密看查询的数据库是否一致
        String md5Hex = DigestUtils.md5Hex(userInfo.getPasswd());
        userInfo.setPasswd(md5Hex);
        //1.获取jedis用于存放redis数据
        String userInfoKey=UserConst.USER_PREFIX+userInfo.getId()+UserConst.USER_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        //调用dao层查询数据库
        UserInfo userInfoResult = userInfoMapper.selectOne(userInfo);
        //判断数据库中是否有这个用户
        //如果该用户存在，则放到redis缓存中，设置有效时间方便查询
        if (userInfoResult!=null){
            //存放用户信息，key一般为:user:userId:info

            //把查询出来的用户对象转为json串以方便存入redis中
            String userInfoJson = JSON.toJSONString(userInfoResult);
            //设置过期时间为3小时并存放Key,Value
            jedis.setex(userInfoKey,UserConst.TIME_OUT,userInfoJson);
            //关闭jedis
            jedis.close();
            //把查询结果返回
            return userInfoResult;

        }else {
            //如果查询出的结果为null,则放回null
            return null;
        }

    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userIdKey=UserConst.USER_PREFIX+userId+UserConst.USER_SUFFIX;
        String userInfoJson = jedis.get(userIdKey);
        if (userInfoJson!=null &&userInfoJson.length()>0){
            //把JSon串转换为Java对象
            UserInfo userInfo = JSON.parseObject(userInfoJson, UserInfo.class);
            //再次更新用户时效
            jedis.expire(userIdKey,UserConst.TIME_OUT);
            jedis.close();
            return  userInfo;
        }
        return null;
    }
}
