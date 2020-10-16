package com.kgc.kmall.kmallpassportweb.test;

import com.kgc.kmall.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2020-10-16 9:34
 */
public class Test01 {

    public static void main(String[] args) {
        Map<String,Object> map=new HashMap<>();
        map.put("memberId",1);
        map.put("nickname","renhaipeng");
        String ip="127.0.0.1";

        String encode = JwtUtil.encode("2020kmall076", map, ip);
        System.out.println(encode);

        Map<String, Object> decode = JwtUtil.decode(encode, "2020kmall076", ip);
        System.out.println(decode);


    }
}
