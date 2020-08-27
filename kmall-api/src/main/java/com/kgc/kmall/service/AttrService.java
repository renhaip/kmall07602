package com.kgc.kmall.service;

import com.kgc.kmall.bean.PmsBaseAttrInfo;
import com.kgc.kmall.bean.PmsBaseAttrValue;

import java.util.List;

/**
 * @author shkstart
 * @create 2020-08-27 19:01
 */
public interface AttrService {
    //根据三级分类id查询属性
    public List<PmsBaseAttrInfo> select(Integer catalog3Id);

    //添加属性
    public Integer  add(PmsBaseAttrInfo pmsBaseAttrInfo);

    //根据id查询属性值
    public  List<PmsBaseAttrValue> getAttrValueList(Integer attrId);

}
