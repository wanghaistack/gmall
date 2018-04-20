package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ListController {
    @Reference
    ListService listService;
    @Reference
    SkuInfoService skuInfoService;
    @RequestMapping("updateES/{skuId}")
    @ResponseBody
    public String getSkuLsInfo(@PathVariable("skuId")String skuId){
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = listService.copySkuToList(skuInfo);
        listService.saveSkuInfo(skuLsInfo);
        return "";
    }
    @RequestMapping("list.html")
    public String getSkuLsResult(SkuLsParams skuLsParams, Map<String,Object> map){
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        map.put("skuLsResult",skuLsResult);
        return "list";

    }
}
