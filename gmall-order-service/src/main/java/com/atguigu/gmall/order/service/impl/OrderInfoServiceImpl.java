package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderInfoService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
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
    public String save(OrderInfo orderInfo) {
        //设置订单创建时间
        orderInfo.setCreateTime(new Date());
        //日期相对于增加一天
        //设置订单过期时间
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE,1);

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
        String orderId=orderInfo.getId();
        //把订单信息存放到redis缓存中
        Jedis jedis = redisUtil.getJedis();
        String orderInfoKey=ORDER_INFO_PREFIX+orderInfo.getUserId()+ORDER_INFO_SUFFIX;
        String orderInfoJson = JSON.toJSONString(orderInfo);
        jedis.setex(orderInfoKey,TIME_OUT,orderInfoJson);
        jedis.close();
        return orderId;
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
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus){
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        //设置进程状态
        orderInfo.setProcessStatus(processStatus);
        //更新订单状态
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        //更新进程状态
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderResult(String orderId) {
        //获取提供者
        //1.获取工厂连接
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        //2.创建连接
        try {
            Connection connection = connectionFactory.createConnection();
            //3.开启连接
            connection.start();
            //创建带有事务的session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建消息队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            //设置发送消息队列的类型
            TextMessage textMessage=new ActiveMQTextMessage();
           //获取访问仓库类型的json
            String mapJson = getMapJson(orderId);
            //设置要发送的文本信息
            textMessage.setText(mapJson);
            //创建生产者
            MessageProducer producer = session.createProducer(order_result_queue);
            //发送信息
            producer.send(textMessage);
            //提交事务
            session.commit();
            session.close();
            connection.close();
            producer.close();
            //纵向代理  横向抽取  13120287773
            //动态数据源
            //动态代理
            //动态代理：字节码重组
            //NIO 1.5 BIO AIO1.7   1000万条数写入数据库中怎么实现
            //一次性hash值
            //netty
            //springbean的生命周期

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    public String getMapJson(String orderId){
        Map<String,Object> map=new HashMap<>();
        OrderInfo orderInfo = getOrderInfo(orderId);
        map.put("orderId",orderId);
        map.put("consignee",orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        Map<String,Object>orderDetailMap=new HashMap<>();
        List<Map> mapList=new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetailMap.put("skuId",orderDetail.getSkuId());
            orderDetailMap.put("skuNum",orderDetail.getSkuNum());
            orderDetailMap.put("skuName",orderDetail.getSkuName());
            mapList.add(orderDetailMap);
        }
        map.put("details",mapList);
        String details = JSON.toJSONString(map);
        return details;
    }


}
