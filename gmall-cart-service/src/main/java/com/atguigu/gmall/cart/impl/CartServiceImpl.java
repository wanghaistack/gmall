package com.atguigu.gmall.cart.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.cartconst.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.constutil.UserConst;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;
    //引用skuInfoService
    @Reference
    SkuInfoService skuInfoService;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public void addCartInfoList(CartInfo cartInfo, String userId) {
        //获取skuId查询skuInfo信息
        String skuId = cartInfo.getSkuId();
        //调用skuInfoService处理业务查询skuInfo信息
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        //添加前先查询数据库中有没有
        CartInfo cartInfoQuery = cartInfoMapper.selectOne(cartInfo);
        //如果有值，则追加数量，更新最新price
        if (cartInfoQuery!=null){
            cartInfoQuery.setSkuNum(cartInfo.getSkuNum()+cartInfoQuery.getSkuNum());
            cartInfoQuery.setCartPrice(skuInfo.getPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoQuery);
            loadRedis(cartInfoQuery,userId);
        }else {
            //如果数据库中没有则添加到数据库中
            //设置用户id便于关联查询
            cartInfo.setUserId(userId);
            //设置默认选中sku的图片
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            //设置价格
            cartInfo.setCartPrice(skuInfo.getPrice());
            //设置skuId
            cartInfo.setSkuId(skuInfo.getId());
            //设置选中的skuName
            cartInfo.setSkuName(skuInfo.getSkuName());
            //添加到数据库中
            cartInfoMapper.insertSelective(cartInfo);
            //设置到redis缓存中
            loadRedis(cartInfo,userId);
        }


    }
    public void  loadRedis(CartInfo cartInfo,String userId){
        //设置到redis缓存中
        Jedis jedis = redisUtil.getJedis();
        //设置cartInfoKey
        String cartInfoKey= CartConst.CART_INFO_PREFIX+userId+CartConst.CART_INFO_SUFFIX;
        String cartInfoJson = JSON.toJSONString(cartInfo);
        jedis.hset(cartInfoKey,cartInfo.getSkuId(),cartInfoJson);
        Long userInfoExpire = jedis.ttl(UserConst.USER_PREFIX+userId+UserConst.USER_SUFFIX);
        jedis.expire(cartInfoKey,userInfoExpire.intValue());
        jedis.close();
    }

}
