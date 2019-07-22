package com.android.skr421.minidouyin.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class FeedResponse {

    @SerializedName("success") private boolean success;
    @SerializedName("feeds") private List<Feed> feeds;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }


}
