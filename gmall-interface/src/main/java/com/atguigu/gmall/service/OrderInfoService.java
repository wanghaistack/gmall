package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

public interface OrderInfoService {
    String getUniquIdentifier(String userId);

    Boolean checkTradeCode(String userId, String tradeCode);

    void deleteTradeCode(String userId);

    void save(OrderInfo orderInfo);

    OrderInfo getOrderInfo(String orederId);

    public List<OrderInfo> getOrderInfoList(String userId);
}
