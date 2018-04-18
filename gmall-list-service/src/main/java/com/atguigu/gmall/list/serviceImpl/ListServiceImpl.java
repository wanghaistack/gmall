package com.atguigu.gmall.list.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.constutil.ListServiceCont;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;

    //保存skuLsInfo
    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo){
        Index index = new Index.Builder(skuLsInfo).index(ListServiceCont.GMALL_INDEX).type(ListServiceCont.TYPE_INDEX).id(skuLsInfo.getId()).build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public  SkuLsInfo copySkuToList(SkuInfo skuInfo) {
        SkuLsInfo skuLsInfo=new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return skuLsInfo;
    }
}
