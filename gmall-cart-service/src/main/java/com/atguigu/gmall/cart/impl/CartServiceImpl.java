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
import tk.mybatis.mapper.entity.Example;

import java.util.*;


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
    public void addCartInfoList(CartInfo cartInfo, String userId,SkuInfo skuInfo) {
        //添加前先查询数据库中有没有
        String skuId = cartInfo.getSkuId();
        CartInfo cartInfos=new CartInfo();
        //因为传递添加的值skuNum与数据库不匹配，所以只传skuId,userId
        cartInfos.setSkuId(skuId);
        cartInfos.setUserId(userId);
        CartInfo cartInfoQuery = cartInfoMapper.selectOne(cartInfos);
        //如果有值，则追加数量，更新最新price
        if (cartInfoQuery!=null){
            cartInfoQuery.setSkuNum(cartInfo.getSkuNum()+cartInfoQuery.getSkuNum());
            cartInfoQuery.setCartPrice(skuInfo.getPrice());
            cartInfoQuery.setSkuPrice(skuInfo.getPrice());
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
            cartInfo.setSkuPrice(skuInfo.getPrice());
            //添加到数据库中
            cartInfoMapper.insertSelective(cartInfo);
            //设置到redis缓存中
            loadRedis(cartInfo,userId);
        }


    }

    @Override
    public List<CartInfo> getCartInfoList(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String cartInfoKey= CartConst.CART_INFO_PREFIX+userId+CartConst.CART_INFO_SUFFIX;
        List<String> cartInfoListJson = jedis.hvals(cartInfoKey);

        //先查询redis,看redis是否有缓存
        if (cartInfoListJson!=null && cartInfoListJson.size()>0){
            List<CartInfo> cartList=new ArrayList<>(cartInfoListJson.size());
            for (String cartJson : cartInfoListJson) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartList.add(cartInfo);
            }
            cartList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });

            return cartList;

        }else {

            List<CartInfo> cartInfoList = loadCartCache(userId);

            return cartInfoList;

        }


    }

    @Override
    public List<CartInfo> mergeCartInfoList(List<CartInfo> cartInfoFromCookie, String userId) {
       CartInfo cartInfo=new CartInfo();
       cartInfo.setUserId(userId);
       boolean isLoad=false;
       //根据userId查询cartInfo信息
        //先循环cookie中list信息，看数据库中是否有值与之相匹配，如果有，则更新数据库，如果没有，则加载到数据库中
        List<CartInfo> cartInfoList = cartInfoMapper.select(cartInfo);
        for (CartInfo cartInfoCookie : cartInfoFromCookie) {
            for (CartInfo cartInfoDB : cartInfoList) {
                //如果商品sku信息相匹配则保存到数据库中
                if (cartInfoCookie.getSkuId().equals(cartInfoDB.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCookie.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isLoad=true;
                }
            }
            if (!isLoad){
                //cookie中设置userId,之前为null
               cartInfoCookie.setUserId(userId);
               //放到数据库中
                cartInfoMapper.insertSelective(cartInfoCookie);
            }
        }
        //更新缓存
        List<CartInfo> cartInfoLists = loadCartCache(userId);
        return cartInfoLists;
    }


    @Override
    public List<CartInfo> loadCartCache(String userId){
        Jedis jedis = redisUtil.getJedis();
        String cartInfoKey= CartConst.CART_INFO_PREFIX+userId+CartConst.CART_INFO_SUFFIX;
        Map <String,String> map=new HashMap<String,String>();
        List<CartInfo> cartInfoList=new ArrayList<>();
        cartInfoList = cartInfoMapper.selectCartInfoListByUserId(Long.parseLong(userId));
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartInfoKey,map);
        jedis.close();
        return cartInfoList;

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
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        //更新数据库缓存
        String cartInfoKey= CartConst.CART_INFO_PREFIX+userId+CartConst.CART_INFO_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        String cartfInfoJson = jedis.hget(cartInfoKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartfInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartInfoChecked = JSON.toJSONString(cartInfo);
        jedis.hset(cartInfoKey,skuId,cartInfoChecked );
        //新增已选中的购物车
        String cartCheckKey=CartConst.CART_CHECKED_PREFIX+userId+CartConst.CART_CHECKED_SUFFIX;
        if ("1".equals(isChecked)){
            jedis.hset(cartCheckKey,skuId,cartInfoChecked);
        }else {
            jedis.hdel(cartCheckKey,skuId);
        }
        jedis.close();


    }
    //在redis缓存中获取所选中的购物车集合
    public List<CartInfo> getCheckedList(String userId){
        String cartCheckKey=CartConst.CART_CHECKED_PREFIX+userId+CartConst.CART_CHECKED_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<CartInfo>cartInfoList=new ArrayList<>();
        List<String> cartCheckJson = jedis.hvals(cartCheckKey);
        if (cartCheckJson!=null && cartCheckJson.size()>0){

            for (String cartJson : cartCheckJson) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }

        }
        jedis.close();
        return cartInfoList;
    }
    //删除购物车
    @Override
    public void deleteCheckedCartInfo(String userId,List<String>skuIds){
        String cartCheckKey=CartConst.CART_CHECKED_PREFIX+userId+CartConst.CART_CHECKED_SUFFIX;
        String cartInfoKey= CartConst.CART_INFO_PREFIX+userId+CartConst.CART_INFO_SUFFIX;
        Example example=new Example(CartInfo.class);
        example.createCriteria().andIn("skuId",skuIds);
        //删除数据库中的购物车信息
        cartInfoMapper.deleteByExample(example);
        Jedis jedis = redisUtil.getJedis();
        for (String skuId : skuIds) {
            jedis.hdel(cartCheckKey,skuId);
            jedis.hdel(cartInfoKey,skuId);
        }
        jedis.sync();
        jedis.close();

    }
}
