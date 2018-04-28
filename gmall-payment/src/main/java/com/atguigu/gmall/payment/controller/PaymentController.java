package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.service.OrderInfoService;
import com.atguigu.gmall.util.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Reference
    OrderInfoService orderInfoService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;

    @RequestMapping(value = "index", method = RequestMethod.GET)
    @LoginRequire
    public String toIndex(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        String userId = (String) request.getAttribute("userId");
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        request.setAttribute("userId",userId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }
    @RequestMapping(value = "/alipay/submit",method =RequestMethod.POST )
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        //用订单号查询订单详情
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        //保存支付信息
        //检查payment中是否有该orderId的单据，如果有，则不在进行保存
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setAlipayTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentService.savePaymentInfo(paymentInfo);
        //制作支付宝参数
        AlipayTradePayRequest alipayTradePayRequest=new AlipayTradePayRequest();
        alipayTradePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        alipayTradePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        Map<String,Object> map=new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("subject",paymentInfo.getSubject());
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        String bizContent = JSON.toJSONString(map);
        alipayTradePayRequest.setBizContent(bizContent);
        String form=null;
        try {
            alipayClient.pageExecute(alipayTradePayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        return form;


    }
    @RequestMapping("/alipay/callback/return")
    public String paymentReturn(){
        return "redirect://"+AlipayConfig.return_order_url;
    }
    /*
    接收异步通知
    1.验证签名
    2.判断成功标志
    3.该单据是否已经处理
    4.修改支付信息状态
    5.通知订单模块
    6.给支付宝回执success
     */
    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap,HttpServletRequest request){
        String sign = request.getParameter("sign");
        boolean isChecked=false;
        try {
           isChecked = AlipaySignature.rsaCheckV2(paramMap, AlipayConfig.alipay_public_key, "UTF-8");
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (!isChecked){
            return "fail";
        }
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfoQuery=new PaymentInfo();
            paymentInfoQuery.setOutTradeNo(out_trade_no);
           PaymentInfo paymentInfo= paymentService.getPaymentInfo(paymentInfoQuery);
            if (paymentInfo.getPaymentStatus()==PaymentStatus.PAID ||paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                //修改状态
                PaymentInfo paymentInfoForUpdate=new PaymentInfo();
                paymentInfoForUpdate.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoForUpdate.setConfirmTime(new Date());
                paymentInfoForUpdate.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoForUpdate);
                return "success";
            }
        }
        return "fail";
    }


}
