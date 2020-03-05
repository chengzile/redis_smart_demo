package com.demo.dao;



import com.demo.entity.UserInfo;
import com.demo.util.DataUtil;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;
import java.util.List;

public class UserInfoDao {
    public UserInfo getUserInfo(int id) throws Exception {
        SqlSession session=null;
        UserInfo userInfo=null;
        try{
        session= DataUtil.getSqlSession();
        String statement = "mapper.UserInfoMapper.getUserInfo";
         userInfo=session.selectOne(statement,id);
        session.commit();
        }catch(Exception e){
            e.printStackTrace();
            session.rollback();
            throw e;
        }finally{
            DataUtil.closeSqlSession();
        }
        return userInfo;
    }
    public void addUserInfo(UserInfo userInfo) throws Exception {
        SqlSession session=null;

        try{
            session= DataUtil.getSqlSession();
            String statement = "mapper.UserInfoMapper.insertUserInfo";

            session.insert(statement,userInfo);
            session.commit();
        }catch(Exception e){
            e.printStackTrace();
            session.rollback();
            throw e;
        }finally{
            DataUtil.closeSqlSession();
        }
    }
    public void addUserInfoList(List<UserInfo> userInfos) throws Exception {
        SqlSession session=null;
        try{
            session= DataUtil.getSqlSession();
            String statement = "mapper.UserInfoMapper.insertBatch";
            session.insert(statement,userInfos);
            session.commit();
        }catch(Exception e){
            e.printStackTrace();
            session.rollback();
            throw e;
        }finally{
            DataUtil.closeSqlSession();
        }
    }
    public void updateUserInfo(UserInfo userInfo) throws Exception {
        SqlSession session=null;

        try{
            session= DataUtil.getSqlSession();
            String statement = "mapper.UserInfoMapper.updateUserInfo";

            session.update(statement,userInfo);
            session.commit();
        }catch(Exception e){
            e.printStackTrace();
            session.rollback();
            throw e;
        }finally{
            DataUtil.closeSqlSession();
        }
    }
}
