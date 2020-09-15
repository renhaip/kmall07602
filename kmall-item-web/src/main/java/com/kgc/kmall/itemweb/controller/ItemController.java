package com.kgc.kmall.itemweb.controller;

import com.alibaba.fastjson.JSON;
import com.kgc.kmall.bean.PmsProductSaleAttr;
import com.kgc.kmall.bean.PmsSkuInfo;
import com.kgc.kmall.bean.PmsSkuSaleAttrValue;
import com.kgc.kmall.service.SkuService;
import com.kgc.kmall.service.SpuService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2020-09-12 15:13
 */
@Controller
public class ItemController {


    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId, Model model) {
        PmsSkuInfo pmsSkuInfo = skuService.selectBySkuId(skuId);
        model.addAttribute("skuInfo", pmsSkuInfo);


        //根据spuid获取销售属性和销售属性值
//        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(pmsSkuInfo.getSpuId().intValue());
//        model.addAttribute("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListIsCheck(pmsSkuInfo.getSpuId(), pmsSkuInfo.getId());
        model.addAttribute("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);


        //根据spuid获得 skuid和销售属性值id的对应关系,并拼接成字符串
        List<PmsSkuInfo> pmsSkuInfos = skuService.selectBySpuId(pmsSkuInfo.getSpuId());

        Map<String, String> map = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            //spuid
            String kid = skuInfo.getId().toString();
            String savid = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuInfo.getSkuSaleAttrValueList()) {
                savid += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            map.put(savid.substring(0, savid.length() - 1), kid);
        }
        System.out.println(map.toString());

        //将sku的销售属性hash表放到页面
        String s = JSON.toJSONString(map);
        model.addAttribute("skuSaleAttrHashJsonStr",s);
        return "item";
    }

}
