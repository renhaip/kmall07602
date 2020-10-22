package com.kgc.kmall.managerservice.service;

import com.alibaba.fastjson.JSON;
import com.kgc.kmall.bean.*;
import com.kgc.kmall.managerservice.mapper.PmsSkuAttrValueMapper;
import com.kgc.kmall.managerservice.mapper.PmsSkuImageMapper;
import com.kgc.kmall.managerservice.mapper.PmsSkuInfoMapper;
import com.kgc.kmall.managerservice.mapper.PmsSkuSaleAttrValueMapper;
import com.kgc.kmall.service.SkuService;
import com.kgc.kmall.util.RedisUtil;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RedissonClient;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

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

    @Resource
    RedisUtil redisUtil;

    @Resource
    RedissonClient redissonClient;

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
   /* public PmsSkuInfo selectBySkuId(Long id) {
        PmsSkuInfo pmsSkuInfo=null;
        String key="sku:"+id+":info";
        Jedis jedis = redisUtil.getJedis();
        String skuInfoJson = jedis.get(key);
        if(skuInfoJson!=null){
            //缓存中有数据
            if(skuInfoJson.equals("empty")){
                return null;
            }

            //从缓存中获取数据
            pmsSkuInfo = JSON.parseObject(skuInfoJson, PmsSkuInfo.class);
        }else{
            String locKey="sku:"+id+":lock";
            String skuLockValue = UUID.randomUUID().toString();
            String lockResult=jedis.set(locKey,skuLockValue,"NX","PX",60*1000);

            if(lockResult!=null){
                //从数据库中获取数据
                pmsSkuInfo = pmsSkuInfoMapper.selectByPrimaryKey(id);

                //防止缓存穿透，从DB中找不到数据也要缓存，但是缓存时间不要太长
                if(pmsSkuInfo!=null){
                    String s = JSON.toJSONString(pmsSkuInfo);
                    //有效期随机，防止缓存雪崩
                    Random random=new Random();
                    int i = random.nextInt(10);
                    jedis.setex(key,i*60*1000,s);
                }else{
                    jedis.setex(key,5*60*1000,"empty");
                }

                //删除分布式锁String skuLockValue2 = jedis.get(locKey);
               *//* if (skuLockValue2!=null&&skuLockValue2.equals(skuLockValue)){
                    jedis.del(locKey);
                }*//*
                String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList(locKey),Collections.singletonList(skuLockValue));
            }else{
                try {
                    Thread.sleep(3*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //抢锁失败回旋调用
                selectBySkuId(id);
            }

        }
        return pmsSkuInfo;
    }*/

    public PmsSkuInfo selectBySkuId(Long id) {
        PmsSkuInfo pmsSkuInfo=null;
        String key="sku:"+id+":info";
        Jedis jedis=redisUtil.getJedis();

        String skuInfoJson=jedis.get(key);

        if(skuInfoJson!=null){
                //缓存中有数据
            if(skuInfoJson.equals("empty")){
                return null;
            }
        //从缓存中获取数据
            pmsSkuInfo = JSON.parseObject(skuInfoJson, PmsSkuInfo.class);
        }else{
            Lock lock=redissonClient.getLock("lock");//声明锁
            lock.lock();//上锁

            //从数据库中获取数据
            pmsSkuInfo = pmsSkuInfoMapper.selectByPrimaryKey(id);
            //防止缓存穿透，从DB中找不到数据也要缓存，但是缓存时间不要太长
            if(pmsSkuInfo!=null){
                String s = JSON.toJSONString(pmsSkuInfo);
                //有效期随机，防止缓存雪崩
                Random random=new Random();
                int i = random.nextInt(10);
//                    jedis.setex(key,i*60*1000,s);
                jedis.set(key,s);
            }else{
                jedis.setex(key,5*60*1000,"empty");
            }
            lock.unlock();// 解锁
        }

        return pmsSkuInfo;

    }



    @Override
    public List<PmsSkuInfo> selectBySpuId(Long spuId) {
        List<PmsSkuInfo>  pmsSkuInfo=null;
        String key="sku:"+spuId+":info";
        Jedis jedis=redisUtil.getJedis();

        String skuInfoJson=jedis.get(key);

        if(skuInfoJson!=null){
            //缓存中有数据
            if(skuInfoJson.equals("empty")){
                return null;
            }
            //从缓存中获取数据
            pmsSkuInfo = JSON.parseArray(skuInfoJson, PmsSkuInfo.class);
        }else{
            Lock lock=redissonClient.getLock("lock");//声明锁
            lock.lock();//上锁

            //从数据库中获取数据
            pmsSkuInfo = pmsSkuInfoMapper.selectBySpuId(spuId);
            //防止缓存穿透，从DB中找不到数据也要缓存，但是缓存时间不要太长
            if(pmsSkuInfo!=null){
                String s = JSON.toJSONString(pmsSkuInfo);
                //有效期随机，防止缓存雪崩
                Random random=new Random();
                int i = random.nextInt(10);
//                    jedis.setex(key,i*60*1000,s);
                jedis.set(key,s);
            }else{
                jedis.setex(key,5*60*1000,"empty");
            }
            lock.unlock();// 解锁
        }

        return pmsSkuInfo;


    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectBySpuId(new Long((long)65));

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSkuAttrValueExample example=new PmsSkuAttrValueExample();
            PmsSkuAttrValueExample.Criteria criteria = example.createCriteria();
            criteria.andSkuIdEqualTo(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.selectByExample(example);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }

        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(Long productSkuId, BigDecimal price) {
        boolean b=false;
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectByPrimaryKey(productSkuId);
        BigDecimal bigDecimal = new BigDecimal(pmsSkuInfo.getPrice());
        if(price.compareTo(bigDecimal)==0){
            b=true;
        }
        return b;
    }
}
