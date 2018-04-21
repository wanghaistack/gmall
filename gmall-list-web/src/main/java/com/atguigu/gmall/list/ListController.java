package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    //用于处理ES的业务服务
    @Reference
    ListService listService;
    //用于处理skuInfo的业务服务
    @Reference
    SkuInfoService skuInfoService;
    @Reference
    ManageService manageService;
    @RequestMapping("updateES/{skuId}")
    @ResponseBody
    public String getSkuLsInfo(@PathVariable("skuId")String skuId){
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = listService.copySkuToList(skuInfo);
        listService.saveSkuInfo(skuLsInfo);
        return "";
    }
    @RequestMapping("list.html")
    public String getSkuLsResult(SkuLsParams skuLsParams, Model model){
        //根据查询条件参数获取ES返回的数据结果
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        //获取返回的skuLsInfo信息
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        //获取传递参数的valueId集合
        List<String> attrValuelList = skuLsResult.getAttrValuelList();
        //获取keyword的值然后放在页面
        String keyword = skuLsParams.getKeyword();
        //获取跳转路径拼接串
        String urlParam = makeUrlParam(skuLsParams);
        //定义总页码
        Long totalPages= (skuLsResult .getTotal()%skuLsParams.getPageSize()==0)?(skuLsResult .getTotal()/skuLsParams.getPageSize()):(skuLsResult .getTotal()/skuLsParams.getPageSize()+1);
        //定义页数
        int pageNo = skuLsParams.getPageNo();
        //定义面包屑
        List<BaseAttrValue> baseAttrValueList=new ArrayList<>();
        //定义attrInfoList
        List<BaseAttrInfo> attrInfoList=null;
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            attrInfoList= manageService.getAttrList(skuLsParams.getCatalog3Id());
        }
        if (attrValuelList!=null &&attrValuelList.size()>0) {
           attrInfoList = manageService.getAttrValueList(attrValuelList);
        }

        for (Iterator<BaseAttrInfo> iterator = attrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo= iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {

                baseAttrValue.setUrlParam(urlParam);
                if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    String[]valueIds= skuLsParams.getValueId();
                    for (String valueId : valueIds) {
                        //如果选中的Id与获取的传递id值相匹配，则删除其属性
                        if (valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            //面包屑
                            BaseAttrValue baseAttrSelected=new BaseAttrValue();
                            //设置baseValue页面显示的名字
                            baseAttrSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            //baseAttrValueList添加元素

                            //如果选中的id值与之相匹配，则不添加url路径
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrSelected.setUrlParam(makeUrlParam);
                            //把参数设置到List中，然后把list设置到域中便于页面显示
                            baseAttrValueList.add(baseAttrSelected);
                    }

                    }
                }
            }

        }
        model.addAttribute("pageNo",pageNo);
        model.addAttribute("totalPages",totalPages);
        model.addAttribute("urlParam",urlParam);
        model.addAttribute("skuLsInfoList",skuLsInfoList);
        model.addAttribute("baseAttrValueList",baseAttrValueList);
        if (keyword!=null && keyword.length()>0){
            model.addAttribute("keyword",keyword);
        }

        model.addAttribute("attrInfoList", attrInfoList);
        return "list";

    }
    private String makeUrlParam(SkuLsParams skuLsParams,String...excludeValueIds){
        String keyword = skuLsParams.getKeyword();
        String urlParam="";
        if (keyword!=null && keyword.length()>0){
            if (urlParam!=null && urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="keyword="+keyword;
        }
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if (urlParam!=null && urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){

            for (int i=0;i<skuLsParams.getValueId().length;i++){

                String valueId=skuLsParams.getValueId()[i];
                //排除选中的属性值
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId .equals(valueId)){
                        continue;
                    }
                }


                if (urlParam!=null && urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }
}
