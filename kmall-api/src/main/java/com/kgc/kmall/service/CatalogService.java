package com.kgc.kmall.service;

import com.kgc.kmall.bean.PmsBaseAttrInfo;
import com.kgc.kmall.bean.PmsBaseCatalog1;
import com.kgc.kmall.bean.PmsBaseCatalog2;
import com.kgc.kmall.bean.PmsBaseCatalog3;

import java.util.List;

/**
 * @author shkstart
 * @create 2020-08-24 15:43
 */
public interface CatalogService {

    public List<PmsBaseCatalog1> getCatalog1();


    public List<PmsBaseCatalog2> getCatalog2(Integer catalog1Id);

    public List<PmsBaseCatalog3> getCatalog3(Integer catalog2Id);



}
