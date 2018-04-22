package com.atguigu.gmall.list.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.constutil.ListServiceCont;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;
    @Autowired
    RedisUtil redisUtil;

    //保存skuLsInfo
    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo){
        Index index = new Index.Builder(skuLsInfo).index(ListServiceCont.GMALL_INDEX).type(ListServiceCont.TYPE_INDEX).id(skuLsInfo.getId()).build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void incrHotScore(String skuId){
        Jedis jedis = redisUtil.getJedis();
        Double hotScore = jedis.zincrby("hotScore", 1, skuId);
        jedis.close();
        if (hotScore%2==0){
            updateHotScore(hotScore,skuId);
        }


    }
    public void updateHotScore(Double hotScore,String skuId){
        String query="{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":\""+hotScore+"\"\n" +
                "  }\n" +
                "}";
        Update update = new Update.Builder(query).index(ListServiceCont.GMALL_INDEX).type(ListServiceCont.TYPE_INDEX).id(skuId).build();
        try {
            DocumentResult result = jestClient.execute(update);
            System.out.println(result.toString());
            jestClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ;
    }
    @Override
    public  SkuLsInfo copySkuToList(SkuInfo skuInfo) {
        SkuLsInfo skuLsInfo=new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return skuLsInfo;
    }
    //构造查询DSL
    public String makeQueryStringForSearch(SkuLsParams skuLsParams){
        //获取DSL查询对象
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        //获取Bool查询对象
        BoolQueryBuilder  boolQueryBuilder=new BoolQueryBuilder();
        //如果查询的名字keyword按条件查询不为空
        if (skuLsParams.getKeyword()!=null){
            //bool下面的Match对象
            MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
           //把Match设置到bool
            boolQueryBuilder.must(matchQueryBuilder);
            //设置高亮显示
            HighlightBuilder highlightBuilder=new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            //因为高亮显示与query是同一级目录，把hightlight放到searchSource里面
            searchSourceBuilder.highlight(highlightBuilder);

        }

        //3级分类
        if (skuLsParams.getCatalog3Id()!=null){
            TermQueryBuilder termQueryBuilder=new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            //平台属性过滤
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId= skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }
        searchSourceBuilder.query(boolQueryBuilder);
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        //把聚合添加进去
        searchSourceBuilder.aggregation(groupby_attr);
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        String query = searchSourceBuilder.toString();

        System.out.println("query************* = " + query);
        return query;
    }
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams){
        String query = makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ListServiceCont.GMALL_INDEX).addType(ListServiceCont.TYPE_INDEX).build();
        SearchResult searchResult=null;
        try {
            searchResult= jestClient.execute(search);
            jestClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

           SkuLsResult skuLsResult= makeResultForSearch(skuLsParams,searchResult);

        return skuLsResult;
    }
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams,SearchResult searchResult){
        SkuLsResult skuLsResult=new SkuLsResult();
        //获取sku列表
        List<SkuLsInfo> skuLsInfoList=new ArrayList<>(skuLsParams.getPageSize());
        List<SearchResult.Hit<SkuLsInfo, Void>> searchResultHits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : searchResultHits) {
            SkuLsInfo skuLsInfo = hit.source;
            if (hit.highlight!=null && hit.highlight.size()>0){
                List<String> skuName = hit.highlight.get("skuName");
                //把带有高亮显示的标签字符串替换skuName
                String s = skuName.get(0);
                skuLsInfo.setSkuName(s);
            }
            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        skuLsResult.setTotal(searchResult.getTotal());
        //取出记录个数并计算出总页数
        Long totalPages=(searchResult.getTotal()%skuLsParams.getPageSize()==0)?(searchResult.getTotal()/skuLsParams.getPageSize()):(searchResult.getTotal()/skuLsParams.getPageSize()+1);
        //把总页数设置到返回结果中
        skuLsResult.setTotalPages(totalPages);
        //取出涉及的属性值id
        System.out.println("searchResult = " + searchResult.getJsonString());
        List<String> attrValueList=new ArrayList<>();
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        if (groupby_attr!=null){
            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                attrValueList.add(key);
            }
            skuLsResult.setAttrValuelList(attrValueList);
        }
        return skuLsResult;
    }

}
