package com.kgc.kmall.service;

import com.kgc.kmall.bean.Member;

import java.util.List;

/**
 * @author shkstart
 * @create 2020-08-19 20:44
 */
public interface MemberService {
    public List<Member> selectAllMember();

    //登陆
    Member login(String username,String password);

    //  增加用户的缓存
    void  addUserToken(String token,Long memberId);
}
