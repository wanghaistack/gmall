package com.atguigu.gmall.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
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
    @Autowired
    AlipayClient alipayClient;

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
    @Override
    public Boolean checkAlipayStatus(PaymentInfo paymentInfo){
        String outTradeNo = paymentInfo.getOutTradeNo();
        PaymentInfo paymentInfoQuery =getPaymentInfo(paymentInfo);
        if (paymentInfoQuery.getPaymentStatus().equals(PaymentStatus.PAID)||paymentInfoQuery.getPaymentStatus().equals(PaymentStatus.ClOSED)){
            return true;
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+outTradeNo+"\"," +

                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())){
                paymentInfoQuery.setPaymentStatus(PaymentStatus.PAID);
                System.out.println("调用成功");
                updatePaymentInfo( outTradeNo, paymentInfoQuery);
                sendPaymentResult(paymentInfoQuery.getOrderId(),"success");
                return true;
            }else {
                return false;
            }

        } else {
            System.out.println("调用失败");
            return false;
        }

    }
}
