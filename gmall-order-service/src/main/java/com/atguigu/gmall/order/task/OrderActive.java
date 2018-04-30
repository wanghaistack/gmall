package com.atguigu.gmall.order.task;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OrderActive {
   @Autowired
    OrderInfoService orderInfoService;
    @Scheduled(cron = "0/15 * * * * ?")
    public void checkOrderExpireInfo(){
        //获取过期订单的集合
        System.out.println("开始扫描过期时间");
        long start = System.currentTimeMillis();
        List<OrderInfo> orderInfoExpireList = orderInfoService.getOrderInfoExpireList();
        for (OrderInfo orderInfo : orderInfoExpireList) {
            System.out.println("扫描完成");
            orderInfoService.setOrderStatus(orderInfo);

        }
        long time=System.currentTimeMillis()-start;
        System.out.println("一共扫描了"+orderInfoExpireList.size()+"个订单,更新时间为："+time+"豪秒");
    }
}
