package com.atguigu.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.service.OrderInfoService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;

@Component
public class OrderConsumer {
    @Reference
    OrderInfoService orderInfoService;
    @JmsListener(destination = "UPDATE_PAYMENT_STATUS",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage ) throws Exception{
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        if ("success".equals(result)){
            orderInfoService.updateOrderStatus(orderId, ProcessStatus.PAID);
        }else {
            orderInfoService.updateOrderStatus(orderId,ProcessStatus.PAY_FAIL);
        }
        orderInfoService.sendOrderResult(orderId);

    }
}
