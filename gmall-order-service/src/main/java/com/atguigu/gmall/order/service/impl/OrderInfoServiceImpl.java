package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    private String ORDER_PREFIX = "order:";
    private String ORDER_SUFFIX = ":code";
    private int TIME_OUT = 60 * 60;

    @Override
    public String getUniquIdentifier(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeKey = ORDER_PREFIX + userId + ORDER_SUFFIX;
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeCodeKey, TIME_OUT, tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public Boolean checkTradeCode(String userId, String tradeCode) {
        String tradeCodeKey = ORDER_PREFIX + userId + ORDER_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeRedis = jedis.get(tradeCodeKey);
        jedis.close();
        if (tradeCodeRedis != null && tradeCodeRedis.length() > 0) {
            if (tradeCodeRedis.equals(tradeCode)) {
                return true;
            } else {
                return false;
            }
        }
        return false;

    }

    @Override
    public void deleteTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeKey = ORDER_PREFIX + userId + ORDER_SUFFIX;
        jedis.del(tradeCodeKey);
        jedis.close();
    }

    @Override
    public void save(OrderInfo orderInfo) {
        //设置订单创建时间
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        //日期相对于增加一天
        calendar.add(Calendar.DATE, 1);
        //设置订单过期时间
        orderInfo.setExpectDeliveryTime(calendar.getTime());
        //设置tradeNo
        String outTradeNo = "ORDER_TRADE" + UUID.randomUUID().toString() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //先加入oredrInfo订单表
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }


    }


}
