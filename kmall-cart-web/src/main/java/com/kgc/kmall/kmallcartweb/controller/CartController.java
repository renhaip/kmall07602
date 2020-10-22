package com.kgc.kmall.kmallcartweb.controller;

import com.alibaba.fastjson.JSON;
import com.kgc.kmall.annotations.LoginRequired;
import com.kgc.kmall.bean.OmsCartItem;
import com.kgc.kmall.bean.PmsSkuInfo;
import com.kgc.kmall.service.CartService;
import com.kgc.kmall.service.SkuService;
import com.kgc.kmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author shkstart
 * @create 2020-10-10 9:39
 */
@Controller
public class CartController {


    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @LoginRequired(value = false)
    @RequestMapping("/addToCart")
    public String addToCart(Long  skuId, Integer num, HttpServletRequest request, HttpServletResponse response){
        List<OmsCartItem> omsCartItems=new ArrayList<>();

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.selectBySkuId(skuId);
        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(new BigDecimal(skuInfo.getPrice()));
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getSpuId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(num);

        //判断用户是否登陆
        String  memberId="";
        if(request.getAttribute("memberId")!=null){
            Integer id=(Integer) request.getAttribute("memberId");
            memberId=id+"";
        }

        //如果用户没有登陆
        if(StringUtils.isBlank(memberId)){

            //cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
                    //如果cookie为空的话
                if(StringUtils.isBlank(cartListCookie)){
                    //cookie为空   为cookie添加数据
                    omsCartItems.add(omsCartItem);
                }else{// cookie不为空
                    omsCartItems= JSON.parseArray(cartListCookie,OmsCartItem.class);

                    //判断添加的购物车数据在cookie中是否存在
                    boolean exist=if_cart_exist(omsCartItems,omsCartItem);
                    if(exist){
                        //之前添加过,更新购物车添加数量
                        //集合传参 需要是用循环来进行遍历
                        for (OmsCartItem cartItem : omsCartItems) {
                            //如果添加的商品和原有购物车的商品重复    则购物车中的商品数量+1
                            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                                cartItem.setQuantity(cartItem.getQuantity()+omsCartItem.getQuantity());
                                break;
                            }
                        }
                    }else{
                        //之前没有添加过购物车  从新王cookie里面在增加
                        omsCartItems.add(omsCartItem);
                    }
                }
            System.out.println(JSON.toJSONString(omsCartItems));
                //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(omsCartItems),60*70*72,true);
        }else{//用户登录了  购物车登陆增加  往数据库里面添加数据
            //思路一：根据用户id查询购物车信息，如果不存在则添加，如果存在判断skuid是否存在，如果不存在则添加，如果存在则修改
            //思路二：根据用户id和skuid查询，如果不存在则添加，如果存在则修改
            // 用户已经登录
            // 从db中查出购物车数据
            OmsCartItem omsCartItemFromDb  = cartService.ifCartExistByUser(memberId, skuId);
            if(omsCartItemFromDb==null){
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(Long.parseLong(memberId));
                omsCartItem.setMemberNickname("test 小明");
                cartService.addCart(omsCartItem);
            }else{
                //该用户添加过当前商品
            }
                Integer quantity = omsCartItemFromDb.getQuantity();
                quantity=quantity+num;
                omsCartItemFromDb.setQuantity(quantity);
                cartService.updateCart(omsCartItemFromDb);

            // 同步缓存
            cartService.flushCartCache(memberId);
        }





        return "redirect:/success.html";
    }



    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b=false;
        for (OmsCartItem cartItem : omsCartItems) {
            Long productSkuId = cartItem.getProductSkuId();
            if(productSkuId.equals(omsCartItem.getProductSkuId())){
                    b=true;
                    break;
            }
        }
        return b;
    }


    @LoginRequired(value = false)
    @RequestMapping("/cartList")
    public String cartList(ModelMap modelMap,HttpServletRequest request){
        List<OmsCartItem> omsCartItems=new ArrayList<>();

        String memberId="";
        if(request.getAttribute("memberId")!=null){
            Integer id=(Integer) request.getAttribute("memberId");
            memberId=id+"";
        }
        //memberId不为空的话  已经登陆  查询db
        if(StringUtils.isNotBlank(memberId)){
            //已经登陆 查询db
            omsCartItems = cartService.cartList(memberId);
        }else{//没有登陆 查询cookie

            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
    /*    for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(new BigDecimal(omsCartItem.getQuantity())));
        }*/

    //计算总价
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.addAttribute("totalAmount",totalAmount);
        modelMap.addAttribute("cartList",omsCartItems);
        return "cartList";
    }


    @LoginRequired(value = false)
    @RequestMapping("/checkCart")
    @ResponseBody
    public Map<String,Object> checkCart(String isChecked, Long skuId, HttpServletRequest request, HttpServletResponse response){
        Map<String,Object> map=new HashMap<>();

        String memberId="";
        if(request.getAttribute("memberId")!=null){
            Integer id=(Integer) request.getAttribute("memberId");
            memberId=id+"";
        }

        if(StringUtils.isNotBlank(memberId)){
                //调用服务,修改状态
            OmsCartItem omsCartItem=new OmsCartItem();
            omsCartItem.setMemberId(Long.parseLong(memberId));
            omsCartItem.setProductSkuId(skuId);
            omsCartItem.setIsChecked(isChecked);
            cartService.checkCart(omsCartItem);

            //计算总价
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            BigDecimal totalAmount=getTotalAmount(omsCartItems);
            map.put("totalAmount",totalAmount);
        }else{
            //没有登陆  查询cookie
            String  cartListCookie= CookieUtil.getCookieValue(request,"cartListCookie",true);
            if(StringUtils.isNotBlank(cartListCookie)){
                List<OmsCartItem> omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);

                //修改
                for (OmsCartItem omsCartItem : omsCartItems) {
                        if(omsCartItem.getProductSkuId()==skuId){
                            omsCartItem.setIsChecked(isChecked);
                            break;
                        }
                }
                //保存cookie
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);

                //计算总价
                BigDecimal totalAmount =getTotalAmount(omsCartItems);
                map.put("totalAmount",totalAmount);

            }
        }
            return map;
    }


    //计算总价
    public BigDecimal  getTotalAmount(List<OmsCartItem> omsCartItems){
        BigDecimal totalMonye=new BigDecimal(0);
        for (OmsCartItem omsCartItem : omsCartItems) {

            //计算小计
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(new BigDecimal(omsCartItem.getQuantity())));

            if(omsCartItem.getIsChecked().equals("1")){
                totalMonye=totalMonye.add(omsCartItem.getTotalPrice());
            }

        }
        return totalMonye;
    }


/*
    @LoginRequired(value = true)
    @RequestMapping("toTrade")
    public String toTrade() {

        return "toTrade";
    }*/




}
