package com.atguigu.gmall.bean;

import java.io.Serializable;
import java.util.List;

public class SkuLsResult implements Serializable {

    List<SkuLsInfo> skuLsInfoList;

    Long total;

    Long totalPages;

    List<String> attrValuelList;

    public List<SkuLsInfo> getSkuLsInfoList() {
        return skuLsInfoList;
    }

    public void setSkuLsInfoList(List<SkuLsInfo> skuLsInfoList) {
        this.skuLsInfoList = skuLsInfoList;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public List<String> getAttrValuelList() {
        return attrValuelList;
    }

    public void setAttrValuelList(List<String> attrValuelList) {
        this.attrValuelList = attrValuelList;
    }
}
