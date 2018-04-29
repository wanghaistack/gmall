package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderInfoService {
    String getUniquIdentifier(String userId);

    Boolean checkTradeCode(String userId, String tradeCode);

    void deleteTradeCode(String userId);

    String save(OrderInfo orderInfo);

    OrderInfo getOrderInfo(String orederId);

    public List<OrderInfo> getOrderInfoList(String userId);

    public void updateOrderStatus(String orderId, ProcessStatus processStatus);

    void sendOrderResult(String orderId);
}
