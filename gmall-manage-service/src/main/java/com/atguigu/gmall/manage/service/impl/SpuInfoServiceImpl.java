package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.manage.mapper.SpuInfoMapper;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SpuInfoServiceImpl implements SpuInfoService{
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        //获取SpuInfo信息集合
        return spuInfoMapper.select(spuInfo);
    }
}
