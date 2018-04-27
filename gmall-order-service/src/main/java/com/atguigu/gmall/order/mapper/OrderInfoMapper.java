package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.bean.OrderInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderInfoMapper extends Mapper<OrderInfo> {
    List<OrderInfo> selectOrderInfoListByUserId(long userId);
}
