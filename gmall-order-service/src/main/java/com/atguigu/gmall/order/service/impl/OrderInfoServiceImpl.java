package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
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
    private  String ORDER_INFO_PREFIX="order";
    private  String ORDER_INFO_SUFFIX=":info";

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
        //把订单信息存放到redis缓存中
        Jedis jedis = redisUtil.getJedis();
        String orderInfoKey=ORDER_INFO_PREFIX+orderInfo.getUserId()+ORDER_INFO_SUFFIX;
        String orderInfoJson = JSON.toJSONString(orderInfo);
        jedis.setex(orderInfoKey,TIME_OUT,orderInfoJson);
        jedis.close();
    }

    @Override
    //获取订单信息详情
    public OrderInfo getOrderInfo(String orederId) {
        //获取orderInfo信息
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orederId);
        OrderDetail orderDetail=new OrderDetail();
        orderDetail.setOrderId(orederId);
        //根据订单信息id,查询订单详细列表集合
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        //把查询中的集合封装到orderInfo中
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    //获取orderInfo的信息
    @Override
    public List<OrderInfo> getOrderInfoList(String userId) {
        //遍历查询耗性能
      /*  OrderInfo orderInfoQuery=new OrderInfo();
        orderInfoQuery.setUserId(userId);
        List<OrderInfo> orderInfoList = orderInfoMapper.select(orderInfoQuery);
        OrderDetail orderDetail=new OrderDetail();
        for (OrderInfo orderInfo : orderInfoList) {
            orderDetail.setOrderId(orderInfo.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
            orderInfo.setOrderDetailList(orderDetailList);
        }*/
      //两表关联查询
       List<OrderInfo>orderInfoList= orderInfoMapper.selectOrderInfoListByUserId(Long.parseLong(userId));
        return orderInfoList;
    }


}
