package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {
    public void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoForUpdate);

    public void sendPaymentResult(String orderId, String result);

    public Boolean checkAlipayStatus(PaymentInfo paymentInfo);

    public void sendActiveMQMessage(String outTradeNo,int deySc,int checkCount);

    public void closePaymentStatus(String orderId);
}
