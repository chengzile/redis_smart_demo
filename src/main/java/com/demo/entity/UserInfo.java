package com.demo.entity;

public class UserInfo {
    private int id;
    private String userName;

    public UserInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserInfo(int id) {
        this.id = id;
        this.userName="username"+id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
