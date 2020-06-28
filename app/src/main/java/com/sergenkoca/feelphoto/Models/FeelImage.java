package com.sergenkoca.feelphoto.Models;

public class FeelImage {

    private String userKey;
    private String url;
    private String displayCount;
    private String tags;

    public FeelImage(){

    }
    public FeelImage(String userKey,String url,String displayCount,String tags){
        this.userKey = userKey;
        this.url = url;
        this.displayCount = displayCount;
        this.tags = tags;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userId) {
        this.userKey = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDisplayCount() {
        return displayCount;
    }

    public void setDisplayCount(String displayCount) {
        this.displayCount = displayCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
