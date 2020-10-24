package com.kgc.kmall.kmallorderservice.service;

import com.kgc.kmall.bean.OmsOrder;
import com.kgc.kmall.bean.OmsOrderExample;
import com.kgc.kmall.bean.OmsOrderItem;
import com.kgc.kmall.kmallorderservice.mapper.OmsOrderItemMapper;
import com.kgc.kmall.kmallorderservice.mapper.OmsOrderMapper;
import com.kgc.kmall.service.OrderService;
import com.kgc.kmall.util.RedisUtil;
import org.apache.dubbo.config.annotation.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author shkstart
 * @create 2020-10-22 10:30
 */
@Service
public class OrderServiceImpl implements OrderService {


    @Resource
    RedisUtil redisUtil;

    @Resource
    OmsOrderMapper omsOrderMapper;

    @Resource
    OmsOrderItemMapper omsOrderItemMapper;

    @Override
    public String genTradeCode(Long memberId) {
        Jedis jedis = redisUtil.getJedis();

        String tradekey = "user:" + memberId + ":tradeCode";

        String tradeCode = UUID.randomUUID().toString();

        jedis.setex(tradekey, 60 * 15, tradeCode);

        jedis.close();
        return tradeCode;
    }

    @Override
    public String checkTradeCode(Long memberId, String tradeCode) {
        Jedis jedis = redisUtil.getJedis();

        String key = "user:" + memberId + ":tradeCode";
        String s = jedis.get(key);
        if (tradeCode.equals(s) && s != null && tradeCode != null) {
            jedis.del(key);
            jedis.close();
            return "success";
        }
        jedis.close();
        return "fail";
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        Long orderId=omsOrder.getId();
        List<OmsOrderItem> omsOrderItems=new ArrayList<>();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            // 删除购物车数据,暂时不进行购物车删除，因为需要频繁的测试
            // cartService.delCart();
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrderExample example=new OmsOrderExample();
        example.createCriteria().andOrderSnEqualTo(outTradeNo);
        List<OmsOrder> omsOrders = omsOrderMapper.selectByExample(example);
        if(omsOrders!=null&&omsOrders.size()>0){
            return omsOrders.get(0);
        }else{
            return null;
        }
    }
}
