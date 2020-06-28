package com.sergenkoca.feelphoto.Models;

public class User {

    private String username;
    private String email;
    private Boolean emailVerify;
    private Boolean active;
    private String congratulateCount;
    private String createdAt;


    public User(){

    }

    public User(String username,String email,Boolean emailVerify,Boolean active,String congratulateCount,String createdAt){
        this.username = username;
        this.email = email;
        this.emailVerify = emailVerify;
        this.active = active;
        this.congratulateCount = congratulateCount;
        this.createdAt= createdAt;
    }

    public String getCongratulateCount() {
        return congratulateCount;
    }

    public void setCongratulateCount(String congratulateCount) {
        this.congratulateCount = congratulateCount;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getEmailVerify() {
        return emailVerify;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setEmailVerify(Boolean emailVerify) {
        this.emailVerify = emailVerify;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
