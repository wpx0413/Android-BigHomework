package com.android.skr421.minidouyin.newtork;

import com.android.skr421.minidouyin.bean.FeedResponse;
import com.android.skr421.minidouyin.bean.PostVideoResponse;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * @author Xavier.S
 * @date 2019.01.17 20:38
 */
public interface IMiniDouyinService {
    //上传视频和封面图
    @Multipart
    @POST("mini_douyin/invoke/video")
    Call<PostVideoResponse> createVideo(
            @Query("student_id") String student_id,
            @Query("user_name") String user_name,
            @Part MultipartBody.Part image,@Part MultipartBody.Part video);

    // 请求图片列表
    @GET("mini_douyin/invoke/video")
    Call<FeedResponse> fetchFeed();
}
