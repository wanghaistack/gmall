package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.constutil.JedisConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        if(skuInfo.getId()==null||skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }
        Example skuSaleAttrValueExample=new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
        Example skuAttrValueExample=new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuAttrValueMapper.deleteByExample(skuAttrValueExample);
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }
        Example skuImageExample=new Example(SkuImage.class);
        skuImageExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuImageMapper.deleteByExample(skuImageExample);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            if(skuImage.getId()!=null&&skuImage.getId().length()==0) {
                skuImage.setId(null);
            }
            skuImageMapper.insertSelective(skuImage);
        }

    }
    @Override
    public SkuInfo getSkuInfo(String skuId) {

        try {
            Thread.sleep(3*1000);
            //自定义从redis工具类中获取jedis对象
            Jedis jedis = redisUtil.getJedis();
            //拼接字符串创建Redis里面的Key值
            String skuInfoKey= JedisConst.SKU_PREFIX+skuId+JedisConst.SKU_SUFFIX;
            //根据key值获取value值
            String skuInfoJson = jedis.get(skuInfoKey);
            //如果返回为空，则调用本地数据库连接
            if (skuInfoJson==null || skuInfoJson.length()==0){
                System.out.println(Thread.currentThread().getName()+"当前缓存中未找到数据");
                //判断是否有人去取锁
                String skuLockKey=JedisConst.SKU_PREFIX+skuId+JedisConst.SKULOCK_SUFFIX;
                String result = jedis.set(skuLockKey, "OK", "NX", "PX", JedisConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(result)){
                    System.out.println(Thread.currentThread().getName()+"获得分布式锁");
                   SkuInfo skuInfo= getSkuInfoDB(skuId);
                   //如果数据库里面没有值，别人恶意攻击的话，直接设置到redis缓存中
                   if (skuInfo==null){
                       jedis.setex(skuInfoKey,JedisConst.TIME_OUT,"empty");
                       return null;
                   }
                     skuInfoJson = JSON.toJSONString(skuInfo);
                     jedis.setex(skuInfoKey, JedisConst.TIME_OUT, skuInfoJson);
                     jedis.close();
                     return skuInfo;


                } else {

                    //假设之后有人取锁后dang掉了，递归调用自己去寻找钥匙。
                    System.out.println(Thread.currentThread().getName()+"未获得分布式锁，开启自旋模式俗称递归.");
                    //等待1秒钟
                    Thread.sleep(1*1000);
                    jedis.close();
                    return getSkuInfo(skuId);
                }

            }else if (skuInfoJson.equals("empty")){
                return null;
            }
            else {
                System.out.println(Thread.currentThread().getName()+"缓存中已有数据正在查询");
                SkuInfo skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                jedis.close();
                return skuInfo;
            }
        }catch (JedisConnectionException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return getSkuInfoDB(skuId);



    }

    @Override
    public SkuInfo getSkuInfoDB(String skuId) {
        System.out.println(Thread.currentThread().getName()+"查询数据库");
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo==null){
            return null;
        } else {
            SkuImage skuImage=new SkuImage();
            skuImage.setSkuId(skuId);
            List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
            skuInfo.setSkuImageList(skuImageList);
            SkuAttrValue skuAttrValue=new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
            skuInfo.setSkuAttrValueList(skuAttrValueList);
            SkuSaleAttrValue skuSaleAttrValue=new SkuSaleAttrValue();
            skuSaleAttrValue.setSkuId(skuId);
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
            skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        }

        return skuInfo;

    }

    @Override
    public List<SkuInfo> getSkuInfoList(String spuId) {
        return skuInfoMapper.selectSkuInfoListBySpuId(Long.parseLong(spuId));
    }

    @Override
    public void deleteSkuInfoBySkuId(String skuId) {
        skuInfoMapper.deleteByPrimaryKey(skuId);
        SkuImage skuImage=new SkuImage();
        skuImage.setSkuId(skuId);
        Example skuSaleAttrValueExample=new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId",skuId);
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);

        Example skuAttrValueExample=new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId",skuId);
        skuAttrValueMapper.deleteByExample(skuAttrValueExample);

        Example skuImageExample=new Example(SkuImage.class);
        skuImageExample.createCriteria().andEqualTo("skuId",skuId);
        skuImageMapper.deleteByExample(skuImageExample);

    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
       List<SkuSaleAttrValue> skuSaleAttrValueList= skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Long.parseLong(spuId));
        return skuSaleAttrValueList;
    }


}
