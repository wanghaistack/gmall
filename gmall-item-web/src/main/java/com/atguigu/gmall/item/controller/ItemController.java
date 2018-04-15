package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ItemController {
    @Reference
    SkuInfoService skuInfoService;
    @RequestMapping("/{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId")String skuId, Model model){
       SkuInfo skuInfo= skuInfoService.getSkuInfo(skuId);
       model.addAttribute("skuInfo",skuInfo);

        return "item";
    }

}
