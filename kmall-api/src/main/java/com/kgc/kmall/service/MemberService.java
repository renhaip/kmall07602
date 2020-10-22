package com.kgc.kmall.service;

import com.kgc.kmall.bean.Member;
import com.kgc.kmall.bean.MemberReceiveAddress;

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


    //根据uid查询  实体对象
    Member checkOauthUser(Long sourceUid);
    //增加
    Integer addOauthUser(Member member);

    List<MemberReceiveAddress> getReceiveAddressByMemberId(Long memberId);

    MemberReceiveAddress getReceiveAddressById(Long receiveAddressId);
}
