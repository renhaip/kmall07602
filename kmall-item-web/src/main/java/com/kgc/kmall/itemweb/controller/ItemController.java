package com.kgc.kmall.itemweb.controller;

import com.kgc.kmall.bean.PmsProductSaleAttr;
import com.kgc.kmall.bean.PmsSkuInfo;
import com.kgc.kmall.service.SkuService;
import com.kgc.kmall.service.SpuService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
    public  String  item(@PathVariable Long skuId, Model model){
        PmsSkuInfo pmsSkuInfo = skuService.selectBySkuId(skuId);
        model.addAttribute("skuInfo",pmsSkuInfo);


        //根据spuid获取销售属性和销售属性值
//        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(pmsSkuInfo.getSpuId().intValue());
//        model.addAttribute("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListIsCheck(pmsSkuInfo.getSpuId(), pmsSkuInfo.getId());
        model.addAttribute("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
        return "item";
    }

}
