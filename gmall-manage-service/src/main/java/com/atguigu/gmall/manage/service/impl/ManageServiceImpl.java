package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> catalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return catalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> catalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return catalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
       BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
       baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoList;

    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        //查询属性的基本信息
        BaseAttrInfo baseAttrInfo=baseAttrInfoMapper.selectByPrimaryKey(attrId);
        //查询属性对应的属性值
        BaseAttrValue baseAttrValue=new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else {
            //防止主键被附上一个空字符串
            if (baseAttrInfo.getId().length()==0){
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

        }
        //把原来的属性值全部清空，免得不知道什么时候添加字段，什么时候修改字段
        BaseAttrValue baseAttrValue=new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);
        //重新插入属性
        if (baseAttrInfo.getAttrValueList()!=null &&baseAttrInfo.getAttrValueList().size()>0){
            for ( BaseAttrValue attrValue :baseAttrInfo.getAttrValueList() ) {
                //防止主键被附上一个空字符串
                if (attrValue.getId()!=null && attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public void deleteAttrInfoByPrimaryKey(String id) {
        baseAttrInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<BaseAttrInfo> getAttrValueList(List<String> attrValuelList) {
        return baseAttrInfoMapper.selectAttrValueListByValueId(attrValuelList);
    }
}
