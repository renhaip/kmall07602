package com.kgc.kmall.kmallpassportweb.controller;

import com.alibaba.fastjson.JSON;
import com.kgc.kmall.bean.Member;
import com.kgc.kmall.service.MemberService;
import com.kgc.kmall.util.HttpclientUtil;
import com.kgc.kmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2020-10-16 9:19
 */
@Controller
public class PassportController {

    @Reference
    MemberService memberService;


    @RequestMapping("/index")
    public String index(String ReturnUrl, Model model){
        model.addAttribute("ReturnUrl",ReturnUrl);
        return  "index";
    }


    @RequestMapping("/login")
    @ResponseBody
    public String  login(String username, String password, HttpServletRequest request){
            //调用用户服务验证用户名和密码

        Member login = memberService.login(username, password);
        if(login!=null){
            //用jwt  制作token
            Long id = login.getId();
            String nickname = login.getNickname();
            Map<String,Object> map=new HashMap<>();
            map.put("id",id);
            map.put("nickname",nickname);

            // 如果使用了nginx，则需要如此获取客户端ip
            //如果没有使用nginx
            String ip = request.getRemoteAddr(); //从 request中获得ip
            if(StringUtils.isBlank(ip)||ip.equals("0:0:0:0:0:0:0:1")){
                ip = "127.0.0.1";
            }
            // 按照设计的算法对参数进行加密后，生成token
            String token = JwtUtil.encode("2020kmall076", map, ip);
            memberService.addUserToken(token,id);
            return token;
        }else{
            return "fail";
        }
    }


    @RequestMapping("/verify")
    @ResponseBody
    public Map<String, Object> verify(String token, String currentIp, HttpServletRequest request){
        // 通过jwt校验token真假
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "2020kmall076", currentIp);
        if(decode!=null){
            map.put("status","success");
            map.put("memberId",decode.get("memberId"));
            map.put("nickname",decode.get("nickname"));
        }else{
            map.put("status","fail");
        }
        return map;
    }

    @RequestMapping("/vlogin")
    public String  vlogin(String code,HttpServletRequest request){


        //code 接收验证码
        //根据授权码获取access_token

        String s3 = "https://api.weibo.com/oauth2/access_token";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","565019016");
        paramMap.put("client_secret","ffd261f78fd2e68317a4e43faae7edef");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.kmall.com:8086/vlogin");
        paramMap.put("code",code);// 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String,String> access_map = JSON.parseObject(access_token_json,Map.class);
        System.out.println(access_map);

        System.out.println(access_map.get("access_token"));
        System.out.println(access_map.get("uid"));

        String access_token = access_map.get("access_token");
        String uid = access_map.get("uid");



        //根据access_token获取用户信息
        // 4 用access_token查询用户信息
        String s4 = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String user_json = HttpclientUtil.doGet(s4);
        Map<String,String> user_map = JSON.parseObject(user_json,Map.class);
        System.out.println(user_map);


        Member umsMember = new Member();
        umsMember.setSourceType(2);
        umsMember.setAccessToken(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid(Long.parseLong((String) user_map.get("idstr")));
        umsMember.setCity((String)user_map.get("location"));
        umsMember.setNickname((String)user_map.get("screen_name"));
        Integer g = 0;
        String gender = user_map.get("gender");
        if(gender.equals("m")){
            g = 1;
        }
        umsMember.setGender(g);


        //判断
        Member member = memberService.checkOauthUser(umsMember.getSourceUid());

        if(member==null){
            memberService.addOauthUser(umsMember);
        }else{
            umsMember=member;
        }


        //用jwt制作token
        String token = "";
        Long id = umsMember.getId();
        String nickname = umsMember.getNickname();

        Map<String,Object> map=new HashMap<>();
        map.put("memberId",id);
        map.put("nickname",nickname);


        // 如果使用了nginx，则需要如此获取客户端ip
        //            String ip = request.getHeader("x-forwarded-for");
        //如果没有使用nginx
        String ip = request.getRemoteAddr();// 从request中获取ip
        if(StringUtils.isBlank(ip)||ip.equals("0:0:0:0:0:0:0:1")){
            ip = "127.0.0.1";
        }

        token  = JwtUtil.encode("2020kmall076", map, ip);

        // 将token存入redis一份
        memberService.addUserToken(token,id);
        return "redirect:http://search.kmall.com:8084/index.html?token="+token;
    }



}
