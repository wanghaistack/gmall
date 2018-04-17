package com.atguigu.gmall.manage.service.impl;

import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;
import com.atguigu.gmall.constutil.HttpclientUtil;
import com.atguigu.gmall.manage.mapper.BaseCatalog1Mapper;
import com.atguigu.gmall.manage.mapper.BaseCatalog2Mapper;
import com.atguigu.gmall.manage.mapper.BaseCatalog3Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HtmlParseServiceImpl {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;




    @Test
    public void parseHtml(){
        String html = HttpclientUtil.doGet("https://www.jd.com/allSort.aspx");
        //System.out.println(html);
        Document document= Jsoup.parse(html);
        Elements elements = document.select("div[class='category-item m']");
        for (Element element : elements) {
            String ctg1Item=element.select(".item-title span").text();
            BaseCatalog1 baseCatalog1=new BaseCatalog1();
            baseCatalog1.setName(ctg1Item);
            baseCatalog1Mapper.insertSelective(baseCatalog1);
             System.out.println(ctg1Item);
            Elements ctg2Items = element.select(".items .clearfix");
            for (Element ctg2Item : ctg2Items) {
                String ctg2text=ctg2Item.select("dt a").text();
                BaseCatalog2 baseCatalog2=new BaseCatalog2();
                baseCatalog2.setName(ctg2text);
                baseCatalog2.setCatalog1Id(baseCatalog1.getId());
                baseCatalog2Mapper.insertSelective(baseCatalog2);
                System.out.println("ctg2text =********* " + ctg2text);
                Elements ctg3Items= ctg2Item.select("dd a");
                for (Element ctg3Item : ctg3Items) {
                    String ctg3text=ctg3Item.text();
                    BaseCatalog3 baseCatalog3=new BaseCatalog3();
                    baseCatalog3.setCatalog2Id(baseCatalog2.getId());
                    baseCatalog3.setName(ctg3text);
                    baseCatalog3Mapper.insertSelective(baseCatalog3);
                    System.out.println("ctg3text =******************** " + ctg3text);
                }
            }
        }
    }
}
