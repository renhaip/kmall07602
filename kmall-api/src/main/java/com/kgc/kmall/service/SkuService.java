package com.kgc.kmall.service;

import com.kgc.kmall.bean.PmsSkuInfo;

/**
 * @author shkstart
 * @create 2020-09-04 20:57
 */
public interface SkuService {


    //增加
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo selectBySkuId(Long id);

}
