package com.sergenkoca.feelphoto.Models;

public class ProfileImage {
    private String userKey;
    private String url;

    public ProfileImage(){

    }

    public ProfileImage(String userKey, String url) {
        this.userKey = userKey;
        this.url = url;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
