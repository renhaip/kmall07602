package com.kgc.kmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.kgc.kmall.annotations.LoginRequired;
import com.kgc.kmall.util.CookieUtil;
import com.kgc.kmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2020-10-16 11:27
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //确保请求是访问控制器的请求
        if (handler.getClass().equals(org.springframework.web.method.HandlerMethod.class)) {
            //获取注解信息
            HandlerMethod hm = (HandlerMethod) handler;
            LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);


            String token="";
            //从cookie中取得token
            String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

            //如果旧的token不为空
            if(StringUtils.isNotBlank(oldToken)){
                //赋值给新的token
                token=oldToken;
            }

            //从request中取得新的cookie
            String newToken=request.getParameter("token");
            if(StringUtils.isNotBlank(newToken)){
                token=newToken;
            }

            //token为空验证不通过
            String result="fail";
            Map<String,String> successMap = new HashMap<>();
            if(StringUtils.isNotBlank(token)){
                //调用验证中心的验证方法进行验证
                String ip = request.getRemoteAddr();//从request中获取ip
                if(StringUtils.isBlank(ip)||ip.equals("0:0:0:0:0:0:0:1")){
                    ip = "127.0.0.1";
                }
                String successJson  = HttpclientUtil.doGet("http://127.0.0.1:8086/verify?token=" + token + "&currentIp=" + ip);
                successMap= JSON.parseObject(successJson,Map.class);
                    result=successMap.get("status");
            }





            // 没有LoginRequired注解不拦截
            if (methodAnnotation == null) {
                System.out.println("拦截器直接放行");
                return true;
            }else{
                //判断value的值是true还是false
                boolean loginSuccess = methodAnnotation.value();// 获得该请求是否必登录成功
                if (loginSuccess) {
                    if(!result.equals("success")){
                        StringBuffer requestURL = request.getRequestURL();
                        response.sendRedirect("http://localhost:8086/index?ReturnUrl="+requestURL);
                        return false;
                    }
                //需要将token携带的用户信息写入
                    request.setAttribute("memberId",successMap.get("memberId"));
                    request.setAttribute("nickname",successMap.get("nickname"));
                    System.out.println("必须登录的");
                }else{
                    if(result.equals("success")){
                        request.setAttribute("memberId", successMap.get("memberId"));
                        request.setAttribute("nickname", successMap.get("nickname"));
                    }
                    System.out.println("没有登陆,也可以放行");
                }
                //验证通过，覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }
        System.out.println("测试拦截器");
        return true;
    }
}
