package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;


@Controller
public class SkuController  {
    @Reference
    SkuInfoService skuInfoService;
    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){

         try {
             skuInfoService.saveSkuInfo(skuInfo);
             return "succeess";
         }catch (Exception e){
             e.printStackTrace();
             return "failed";
         }

    }
    @RequestMapping("getSkuList")
    @ResponseBody
    public List<SkuInfo> getSkuList(@RequestParam Map <String,String> map){
        String spuId = map.get("spuId");
       List<SkuInfo> skuInfoList = skuInfoService.getSkuInfoList(spuId);
       return skuInfoList;
    }

}
