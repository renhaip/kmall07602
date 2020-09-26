package com.kgc.kmall.service;

import com.kgc.kmall.bean.PmsSearchSkuInfo;
import com.kgc.kmall.bean.PmsSearchSkuParam;

import java.util.List;

/**
 * @author shkstart
 * @create 2020-09-21 21:48
 */
public interface SearchService {


    List<PmsSearchSkuInfo> list(PmsSearchSkuParam pmsSearchSkuParam);
}
