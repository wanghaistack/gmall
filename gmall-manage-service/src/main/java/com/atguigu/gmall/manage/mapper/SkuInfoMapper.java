package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuInfoMapper extends Mapper<SkuInfo> {
    List<SkuInfo> selectSkuInfoListBySpuId(long spuId);
}
