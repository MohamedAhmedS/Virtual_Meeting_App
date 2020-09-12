package com.example.virtualmeetingapp.Model;

public class User {
    private String id;
    private String userType;
    private String userName;
    private String phoneNo;
    private String onlineStatus;
    private String typingTo;
    private String token;


    public User(String id, String userType, String userName, String phoneNo, String onlineStatus, String typingTo, String token) {
        this.id = id;
        this.userType = userType;
        this.userName = userName;
        this.phoneNo = phoneNo;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.token = token;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    }

