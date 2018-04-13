package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
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
    @RequestMapping("getSpuInfoList")
    @ResponseBody
    public List<SpuInfo> getSpuList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        List<SpuInfo> spuInfoList=spuInfoService.getSpuList(catalog3Id);
        return spuInfoList;
    }
    //获取spuImg的集合并返回到页面中
    @RequestMapping("getSpuImgList")
    @ResponseBody
    public List<SpuImage> getSpuImgList(@RequestParam ("spuId") String spuId){
        List<SpuImage>spuImageList =spuInfoService.getSpuImgList(spuId);
        return spuImageList;
    }
    @RequestMapping("getAttrInfoList")
    @ResponseBody
    public List<BaseAttrInfo>getAttrInfoList(@RequestParam Map<String,String>map){
        String ctg3Id = map.get("ctg3Id");
        List<BaseAttrInfo> baseAttrInfoList= spuInfoService.getAttrInfoList(ctg3Id);
        return baseAttrInfoList;
    }
    @RequestMapping("getSpuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(@RequestParam Map<String,String>map){
        String spuId = map.get("spuId");

       List<SpuSaleAttr> spuSaleAttrList= spuInfoService.getSpuSaleAttrList(spuId);
       return spuSaleAttrList;
    }


}
