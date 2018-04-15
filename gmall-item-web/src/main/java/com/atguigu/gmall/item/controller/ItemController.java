package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    SkuInfoService skuInfoService;
    @Reference
    SpuInfoService spuInfoService;
    @RequestMapping("/{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId")String skuId, Model model){
       SkuInfo skuInfo= skuInfoService.getSkuInfo(skuId);
        List<SpuSaleAttr> spuSaleAttrList=spuInfoService.getSpuSaleAttrListes(skuInfo);
       model.addAttribute("skuInfo",skuInfo);
       model.addAttribute("spuSaleAttrList",spuSaleAttrList);
        List<SkuSaleAttrValue>skuSaleAttrValueList= skuInfoService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        String valueIdKey="";
        Map vauleIdMap=new HashMap();
        for (int i=0;i<skuSaleAttrValueList.size();i++){
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            if (valueIdKey.length()!=0){
                valueIdKey+="|";
            }
            valueIdKey+=skuSaleAttrValue.getSaleAttrValueId();
            //判断如果是最后一个，设置到map中重新拼接。如果sku_id不等，也重新拼接
            if ((i+1)==skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                vauleIdMap.put(valueIdKey,skuSaleAttrValue.getSkuId());
                valueIdKey="";
            }
        }
        //把map转换为Json串
        String valueIdSkuJson = JSON.toJSONString(vauleIdMap);
        model.addAttribute("valueIdSkuJson",valueIdSkuJson);
        return "item";
    }

}
