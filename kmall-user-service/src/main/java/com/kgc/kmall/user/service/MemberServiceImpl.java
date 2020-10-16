package com.kgc.kmall.user.service;

import com.alibaba.fastjson.JSON;
import com.kgc.kmall.bean.Member;
import com.kgc.kmall.bean.MemberExample;
import com.kgc.kmall.service.MemberService;
import com.kgc.kmall.user.mapper.MemberMapper;
import com.kgc.kmall.user.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author shkstart
 * @create 2020-08-19 20:44
 */
@Service
public class MemberServiceImpl implements MemberService {

    @Resource
    MemberMapper memberMapper;

    @Resource
    RedisUtil redisUtil;

    @Override
    public List<Member> selectAllMember() {
        List<Member> members = memberMapper.selectByExample(null);
        return members;
    }

    @Override
    public Member login(String username, String password) {
        Jedis jedis = redisUtil.getJedis();


        try {
            if (jedis != null) {
                //从缓存中查询
                String umsMemberStr = jedis.get("user:" + username + ":info");
                if (StringUtils.isNotBlank(umsMemberStr)) {
                    //密码正确
                    Member member = JSON.parseObject(umsMemberStr, Member.class);
                    if (member.getPassword().equals(password)) {
                        return member;
                    }
                } else {  //缓存中没有  从数据库中查询
                    Member umsMemberFromDb = loginFromDb(username, password);
                    if (umsMemberFromDb != null) {
                        jedis.setex("user:" + username + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));
                    }
                    return umsMemberFromDb;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public void addUserToken(String token, Long memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + memberId + ":token", 60 * 60 * 2, token);
        jedis.close();
    }

    public Member loginFromDb(String username, String password) {
        MemberExample example = new MemberExample();
        MemberExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(username).andPasswordEqualTo(password);
        List<Member> members = memberMapper.selectByExample(example);
        if (members.size() > 0) {
            return members.get(0);
        }
        return null;
    }
}
