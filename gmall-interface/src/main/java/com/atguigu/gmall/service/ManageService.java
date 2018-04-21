package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;

import java.util.List;

public interface ManageService {
    //获取第一类
    public List<BaseCatalog1> getCatalog1();
    //根据1节点的id获取2的属性集合
    public List<BaseCatalog2> getCatalog2(String catalog1Id);
    //根据2节点的id获取3的属性集合
    public List<BaseCatalog3> getCatalog3(String catalog2Id);
   //根据3的id集合，获取信息集合
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

   public BaseAttrInfo getAttrInfo(String attrId);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public void deleteAttrInfoByPrimaryKey(String id);

    List<BaseAttrInfo> getAttrValueList(List<String> attrValuelList);
}
