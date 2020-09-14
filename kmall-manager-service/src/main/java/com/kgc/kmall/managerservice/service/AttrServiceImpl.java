package com.kgc.kmall.managerservice.service;

import com.kgc.kmall.bean.PmsBaseAttrInfo;
import com.kgc.kmall.bean.PmsBaseAttrInfoExample;
import com.kgc.kmall.bean.PmsBaseAttrValue;
import com.kgc.kmall.bean.PmsBaseAttrValueExample;
import com.kgc.kmall.managerservice.mapper.PmsBaseAttrInfoMapper;
import com.kgc.kmall.managerservice.mapper.PmsBaseAttrValueMapper;
import com.kgc.kmall.service.AttrService;
import org.apache.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author shkstart
 * @create 2020-08-27 19:01
 */
@Service
public class AttrServiceImpl implements AttrService {


    @Resource
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Resource
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> select(Integer catalog3Id) {
            PmsBaseAttrInfoExample example=new PmsBaseAttrInfoExample();
            PmsBaseAttrInfoExample.Criteria criteria = example.createCriteria();
            criteria.andCatalog3IdEqualTo(catalog3Id.longValue());
            List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectByExample(example);

            //查询平台属性值
        for (PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfos) {
            PmsBaseAttrValueExample example1=new PmsBaseAttrValueExample();
            PmsBaseAttrValueExample.Criteria criteria1 = example1.createCriteria();
            criteria1.andAttrIdEqualTo(pmsBaseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.selectByExample(example1);
            pmsBaseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }
            return pmsBaseAttrInfos;
    }

    @Override
    public Integer add(PmsBaseAttrInfo pmsBaseAttrInfo) {
            int  i=0;
            //判断是添加还是修改  属性
        if(pmsBaseAttrInfo.getId()==null){
            //添加属性
            i=pmsBaseAttrInfoMapper.insert(pmsBaseAttrInfo);
        }else{
            //修改属性
            i=pmsBaseAttrInfoMapper.updateByPrimaryKey(pmsBaseAttrInfo);
        }

        //删除属性值
        PmsBaseAttrValueExample example=new PmsBaseAttrValueExample();
        PmsBaseAttrValueExample.Criteria criteria = example.createCriteria();
        criteria.andAttrIdEqualTo(pmsBaseAttrInfo.getId());
         i= pmsBaseAttrValueMapper.deleteByExample(example);


         //添加属性值
            if(pmsBaseAttrInfo.getAttrValueList().size()>0){
              i=  pmsBaseAttrValueMapper.insertBatch(pmsBaseAttrInfo.getId(),pmsBaseAttrInfo.getAttrValueList());
            }

        return i;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(Integer attrId) {
        PmsBaseAttrValueExample example=new PmsBaseAttrValueExample();
        PmsBaseAttrValueExample.Criteria criteria = example.createCriteria();
        criteria.andAttrIdEqualTo(attrId.longValue());
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.selectByExample(example);
        return pmsBaseAttrValues;
    }
}
