package com.android.skr421.minidouyin.bean;

import com.google.gson.annotations.SerializedName;


public class PostVideoResponse {


    @SerializedName("success") private boolean success;
    @SerializedName("item") private  Item item;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public static class Item{
        @SerializedName("student_id") private String student_id;
        @SerializedName("user_name") private String user_name;
        @SerializedName("_id") private String _id;
        @SerializedName("image_url") private String image_url;
        @SerializedName("video_url") private String video_url;

        public void set_id(String _id) {
            this._id = _id;
        }

        public String get_id() {
            return _id;
        }

        public void setStudent_id(String student_id) {
            this.student_id = student_id;
        }

        public String getStudent_id() {
            return student_id;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public String getUser_name() {
            return user_name;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setVideo_url(String video_url) {
            this.video_url = video_url;
        }

        public String getVideo_url() {
            return video_url;
        }
    }
}
