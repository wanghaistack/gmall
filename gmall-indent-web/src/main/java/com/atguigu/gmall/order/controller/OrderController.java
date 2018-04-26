package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderInfoService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.atguigu.gmall.bean.enums.OrderStatus.UNPAID;

@Controller
public class OrderController {
    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    @Reference
    OrderInfoService orderInfoService;
    @Reference
    SkuInfoService skuInfoService;

    @ResponseBody
    @RequestMapping("/order/{userId}")
    public List<UserAddress> initOrder(@PathVariable("userId") String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        JSON.toJSONString("");
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        return userAddressList;
    }

    @RequestMapping(value = "trade", method = RequestMethod.GET)
    @LoginRequire
    public String toTrade(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        //获取用户地址信息
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList", userAddressList);
        //获取用户提交的购物车的信息
        List<CartInfo> cartInfoList = cartService.getCartInfoList(userId);
        //定义订单详情表集合
        List<OrderDetail> orderDetailList = new ArrayList<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList", orderDetailList);
        //获取应付总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        //把应付总金额设置到域中
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        //当提交订单结算时，为了防止用户重复提交，设置唯一标识符判断用户是否已提交。
        //防止用户回退提交，应设置随机唯一标识，前台页面设置一个隐藏的，后台与之进行对比，匹配则跳转，不匹配则友好提示
        //放在redis缓存中，以便跳转之后验证
        String tradeCode = orderInfoService.getUniquIdentifier(userId);
        //生成唯一标识符，uuid,并放在域中
        request.setAttribute("tradeCode", tradeCode);
        return "trade";

    }

    @RequestMapping(value = "submitOrder", method = RequestMethod.POST)
    @LoginRequire
    //orderInfo用于接收用户传递的参数
    public String toList(OrderInfo orderInfo, HttpServletRequest request) {
        //获取userId
        String userId = (String) request.getAttribute("userId");
        //获取验证唯一标识，如果认证通过则通过，如果没有，则返回错误页面,主要防止重复提交
        String tradeCode = request.getParameter("tradeCode");
        //调用service进行校验
        Boolean flag = orderInfoService.checkTradeCode(userId, tradeCode);
        if (!flag){
           String errMsg="由于订单在提交过程中引起的不便，请重新提交，谢谢合作！";
           request.setAttribute("errMsg",errMsg);
           return "tradeFail";
        }
        //设置订单状态未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //设置进度状态为未支付
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        //设置总金额
        orderInfo.sumTotalAmount();
        //设置订单里的用户Id,该订单属于哪个用户
        orderInfo.setUserId(userId);
        //检验，验证价格是否更改
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //根据订单详细列表获取skuId商品信息判断价格是否更改
            SkuInfo skuInfo = skuInfoService.getSkuInfo(orderDetail.getSkuId());
            if (!orderDetail.getOrderPrice().equals(skuInfo.getPrice())){
                String errMsg="你选择的商品有价格变动，请重新选择， 给你带来的不便敬请谅解！";
                request.setAttribute("errMsg",errMsg);
                return "tradeFail";
            }
        }
        //如果相匹配，则存放到数据库中
        orderInfoService.save(orderInfo);
        //删除唯一标识
        orderInfoService.deleteTradeCode(userId);
        return "redirect://payment.gmall.com/index";


    }


}
