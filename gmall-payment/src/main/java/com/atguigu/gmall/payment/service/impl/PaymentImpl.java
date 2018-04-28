package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
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
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfoForUpdate,example);
    }
}
