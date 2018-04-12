package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class SpuController {
    @Reference
    SpuInfoService spuInfoService;
    @RequestMapping("spuListPage")
    public String toSpuListPage(){
        return "spuListPage";
    }
    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuInfoList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        SpuInfo spuInfo=new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = spuInfoService.getSpuInfoList(spuInfo);
        return spuInfoList;
    }
    @RequestMapping("getBaseSaleAttrrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrrList(){
       List<BaseSaleAttr> baseSaleAttrList= spuInfoService.getBaseSaleAttrrList();
       return baseSaleAttrList;
    }
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        spuInfoService.saveSpuInfo(spuInfo);
        return "success";
    }

}
