package com.atguigu.gmall.manage.controller;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.service.SkuInfoService;
import jdk.nashorn.internal.ir.annotations.Reference;
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

}
