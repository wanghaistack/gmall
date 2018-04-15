package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SpuInfoServiceImpl implements SpuInfoService{
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        //获取SpuInfo信息集合
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectAll();
        return baseSaleAttrList;
    }
    /*
    修改和保存性质一样，唯一区别根据条件判断spuId是否为Null，如果为Null，则视为保存添加
    如果不为Null，则视为修改。
     */
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        if (spuInfo.getId()!=null && spuInfo.getId().length()==0){
            spuInfo.setId(null);
        }
        spuInfoMapper.insert(spuInfo);
        //获取SpuImage集合
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
        //获取SpuSaleAttr集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            //获取SpuSaleAttrValue集合
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrId(spuSaleAttr.getSaleAttrId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }

        }
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo=new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    //获取spuImgList图片内容属性的集合
    @Override
    public List<SpuImage> getSpuImgList(String spuId) {
        SpuImage spuImage=new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String ctg3Id) {
      List<BaseAttrInfo>baseAttrInfoList= baseAttrInfoMapper.getAttrInfoListByctg3Id(Long.parseLong(ctg3Id));
        return baseAttrInfoList;

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
       List<SpuSaleAttr> spuSaleAttrList= spuSaleAttrMapper.getSpuSaleAttrList(Long.parseLong(spuId));
       return spuSaleAttrList;
    }
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListes(SkuInfo skuInfo) {
        List<SpuSaleAttr>spuSaleAttrList =spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(skuInfo.getId()),Long.parseLong(skuInfo.getSpuId()));
        return spuSaleAttrList;
    }

}
