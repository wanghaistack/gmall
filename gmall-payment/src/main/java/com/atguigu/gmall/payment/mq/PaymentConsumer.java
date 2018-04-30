package com.atguigu.gmall.payment.mq;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "TEST_SEND_ACTIVE_STATUS",containerFactory = "jmsQueueListener")
    public void consumerActivePaymentInfo(MapMessage mapMessage){
        try {
            String outTradeNo = mapMessage.getString("outTradeNo");
            int deySc = mapMessage.getInt("deySc");
            int checkCount = mapMessage.getInt("checkCount");
            PaymentInfo paymentInfo=new PaymentInfo();
            paymentInfo.setOutTradeNo(outTradeNo);
            System.out.println("开始检查支付结果");
            Boolean result = paymentService.checkAlipayStatus(paymentInfo);
            System.out.println("支付结果为："+result);
            if (!result && checkCount>0){
                System.out.println("再次发送延迟队列"+checkCount);
                paymentService.sendActiveMQMessage(outTradeNo,deySc,checkCount-1);

            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
