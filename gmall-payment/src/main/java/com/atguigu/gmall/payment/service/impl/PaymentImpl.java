package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;


@Service
public class PaymentImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQuery);
        return paymentInfo;
    }

    @Override
    public void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoForUpdate) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfoForUpdate, example);
    }
    @Override
    public void sendPaymentResult(String orderId, String result) {
        //调用serviceUtil中的activeMQUtil
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            //获取连接对象
            Connection connection = connectionFactory.createConnection();
            //开启连接
            connection.start();
            //制造session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建消息队列名称
            Queue queueQuery = session.createQueue("UPDATE_PAYMENT_STATUS");
            //创建生产者 producer
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", orderId);
            mapMessage.setString("result", result);
            MessageProducer producer = session.createProducer(queueQuery);
            producer.send(mapMessage);
            session.commit();
            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
