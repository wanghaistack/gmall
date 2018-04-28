package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {
    public void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoForUpdate);
}
