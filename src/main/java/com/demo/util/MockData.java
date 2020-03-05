package com.demo.util;

import com.demo.dao.UserInfoDao;
import com.demo.entity.UserInfo;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

public class MockData {
  static UserInfoDao userInfoDao=new UserInfoDao();
  static JedisPool jedisPool=new JedisPool("192.168.25.133",6301);
    public static void mockDataBase(){


        try {
            List<UserInfo> userInfos=new ArrayList<UserInfo>();
            for(int i=0;i<10000;i++){
                UserInfo userInfo=new UserInfo(i);
                userInfos.add(userInfo);
            }
            userInfoDao.addUserInfoList(userInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void mockRedis(){
        GenericObjectPoolConfig config=new GenericObjectPoolConfig();
        config.setMaxIdle(8);
    }
    public static void main(String[] args){
        mockDataBase();
    }
}
