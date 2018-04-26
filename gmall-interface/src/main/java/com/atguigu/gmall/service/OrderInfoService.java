package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderInfoService {
    String getUniquIdentifier(String userId);

    Boolean checkTradeCode(String userId, String tradeCode);

    void deleteTradeCode(String userId);

    void save(OrderInfo orderInfo);
}
