package com.kgc.kmall;

import com.kgc.kmall.bean.PmsSearchSkuInfo;
import com.kgc.kmall.bean.PmsSkuInfo;
import com.kgc.kmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.dubbo.config.annotation.Reference;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class KmallSearchServiceApplicationTests {


    @Reference
    SkuService skuService;

    @Resource
    JestClient jestClient;
    @Test
    void contextLoads() {

        List<PmsSkuInfo> allSku = skuService.getAllSku();
        List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : allSku) {
            PmsSearchSkuInfo pmsSearchSkuInfo=new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfo.setProductId(pmsSkuInfo.getSpuId());
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            System.out.println(pmsSearchSkuInfo.toString());
            Index index=new Index.Builder(pmsSearchSkuInfo).index("kmall").type("pmsSkuInfo")
                    .id(pmsSearchSkuInfo.getId()+"").build();
            try {
                jestClient.execute(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //使用jest实现查询
    @Test
    void test01(){
        String str="{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": \n" +
                "        {\n" +
                "          \"match\": {\n" +
                "            \"skuName\": \"iphone\"\n" +
                "          }\n" +
                "        }\n" +
                "      \n" +
                "    }\n" +
                "  }\n" +
                "}";

        Search  search=new Search.Builder(str).addIndex("kmall").addType("PmsSkuInfo").build();

        try {
            SearchResult searchResult=jestClient.execute(search);
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;
                System.out.println(source.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //dsl查询
    @Test
    void  test02(){
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();

        TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId","43");
        TermsQueryBuilder termsQueryBuilder=new TermsQueryBuilder("skuAttrValueList.valueId","39","48","51");

        boolQueryBuilder.filter(termQueryBuilder);
        boolQueryBuilder.filter(termsQueryBuilder);

        MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName","iphone");
        boolQueryBuilder.must(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort("id", SortOrder.DESC);
        System.out.println(searchSourceBuilder.toString());

        Search search=new Search.Builder(searchSourceBuilder.toString()).addType("kmall").addType("PmsSkuInfo").build();

        List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();
        try {
            SearchResult searchResult = jestClient.execute(search);
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;
                pmsSearchSkuInfos.add(source);
                System.out.println(pmsSearchSkuInfos.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
