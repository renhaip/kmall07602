package com.kgc.kmall.managerservice.service;

import com.kgc.kmall.bean.PmsSkuAttrValue;
import com.kgc.kmall.bean.PmsSkuImage;
import com.kgc.kmall.bean.PmsSkuInfo;
import com.kgc.kmall.bean.PmsSkuSaleAttrValue;
import com.kgc.kmall.managerservice.mapper.PmsSkuAttrValueMapper;
import com.kgc.kmall.managerservice.mapper.PmsSkuImageMapper;
import com.kgc.kmall.managerservice.mapper.PmsSkuInfoMapper;
import com.kgc.kmall.managerservice.mapper.PmsSkuSaleAttrValueMapper;
import com.kgc.kmall.service.SkuService;
import org.apache.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author shkstart
 * @create 2020-09-04 20:10
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Resource
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Resource
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Resource
    PmsSkuImageMapper pmsSkuImageMapper;

    @Resource
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        System.out.println(pmsSkuInfo.toString());
        int insert = pmsSkuInfoMapper.insert(pmsSkuInfo);
        Long pmsSkuInfoId = pmsSkuInfo.getId();

        //平台属性关系
       List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
       /*  if(skuAttrValueList!=null&&skuAttrValueList.size()>0){
            pmsSkuAttrValueMapper.addSkuAttrValueList(pmsSkuInfoId.intValue(),pmsSkuInfo.getSkuAttrValueList());
        }*/
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfoId);
            pmsSkuAttrValueMapper.insert(pmsSkuAttrValue);
        }

        //图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
//        if(skuImageList!=null&&skuImageList.size()>0){
//            pmsSkuImageMapper.addPmsSkuImage(pmsSkuInfoId.intValue(),pmsSkuInfo.getSkuImageList());
//        }
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfoId);
            pmsSkuImageMapper.insert(pmsSkuImage);
        }

        //销售属性关系
     List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
       /*    if(skuSaleAttrValueList!=null&&skuSaleAttrValueList.size()>0){
            pmsSkuSaleAttrValueMapper.addPmsSkuSaleAttrValue(pmsSkuInfoId.intValue(),pmsSkuInfo.getSkuSaleAttrValueList());
        }*/
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfoId);
            pmsSkuSaleAttrValueMapper.insert(pmsSkuSaleAttrValue);
        }
        return String.valueOf(insert);
    }

    @Override
    public PmsSkuInfo selectBySkuId(Long id) {
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectByPrimaryKey(id);
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> selectBySpuId(Long spuId) {
        return pmsSkuInfoMapper.selectBySpuId(spuId);
    }
}
