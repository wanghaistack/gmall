package com.atguigu.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.service.OrderInfoService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {
    @Reference
    OrderInfoService orderInfoService;
    @JmsListener(destination = "UPDATE_PAYMENT_STATUS",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage ) throws Exception{
        //获取消息队列中传递的参数
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        if ("success".equals(result)){
            orderInfoService.updateOrderStatus(orderId, ProcessStatus.PAID);
            //给库存发布信息，减库存验库
            orderInfoService.sendOrderResult(orderId);
            orderInfoService.updateOrderStatus(orderId,ProcessStatus.WAITING_DELEVER);
        }else {
            orderInfoService.updateOrderStatus(orderId,ProcessStatus.PAY_FAIL);
        }


    }
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory ="jmsQueueListener" )
    public void updateOrderInfoStatus(MapMessage mapMessage){
        try {
            String orderId = mapMessage.getString("orderId");
            String status = mapMessage.getString("status");
            if ("DEDUCTED".equals(status)){
                orderInfoService.updateOrderStatus(orderId,ProcessStatus.DELEVERED);
            }
            if ("OUT_OF_STOCK".equals(status)){
                orderInfoService.updateOrderStatus(orderId,ProcessStatus.STOCK_EXCEPTION);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
