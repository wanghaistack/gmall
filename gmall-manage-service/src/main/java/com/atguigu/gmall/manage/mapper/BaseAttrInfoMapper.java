package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    List<BaseAttrInfo> getAttrInfoListByctg3Id(long ctg3Id);

    List<BaseAttrInfo> selectAttrValueListByValueId(@Param("attrValuelList") List<String> attrValuelList);
}

