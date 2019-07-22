package com.android.skr421.minidouyin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.android.skr421.minidouyin.bean.Feed;
import com.android.skr421.minidouyin.bean.FeedResponse;
import com.android.skr421.minidouyin.newtork.IMiniDouyinService;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView videoList;
    private Button refresh_btn;
    private Button record_btn;
    private Button post_btn;
    private Button mine_btn;
    private Retrofit retrofit;
    Call<FeedResponse> feedResponseCall;
    private List<Feed> mFeeds = new ArrayList<>();
    private static final String[] mPermissions=new String[]{
            Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE
    };
    private final int REQUEST_NETWORK=1;
    private static final String[] cameraPermissions=new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CAMERA_PERMISSIONS = 2;

    RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.ic_launcher_foreground)//图片加载出来前，显示的图片
            .fallback( R.drawable.ic_launcher_foreground) //url为空的时候,显示的图片
            .error(R.mipmap.ic_launcher);//图片加载失败后，显示的图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBtn();
        initRecyclerView();
        //获取网络权限
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mPermissions, REQUEST_NETWORK);
            }
        }
        else {
            fetchFeed();
        }
    }

    //初始化按钮
    private void initBtn()
    {
        LinearLayout threeBtn=findViewById(R.id.three_btn);
        refresh_btn=findViewById(R.id.refresh_btn);
        record_btn=findViewById(R.id.record_btn);
        post_btn=findViewById(R.id.post_btn);
        mine_btn=findViewById(R.id.mine_btn);
        threeBtn.setBackgroundColor(Color.BLACK);
        record_btn.setBackgroundColor(Color.BLACK);
        refresh_btn.setBackgroundColor(Color.BLACK);
        post_btn.setBackgroundColor(Color.BLACK);
        mine_btn.setBackgroundColor(Color.BLACK);
        record_btn.setTextColor(Color.WHITE);
        refresh_btn.setTextColor(Color.WHITE);
        post_btn.setTextColor(Color.WHITE);
        mine_btn.setTextColor(Color.WHITE);
        //点击刷新按钮刷新列表
        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchFeed();
            }
        });
        //发布视频
        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedResponseCall.cancel();
                Intent postIntent=new Intent(MainActivity.this,PostActivity.class);
                startActivity(postIntent);
            }
        });
        //个人信息
        mine_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent infoIntent=new Intent(MainActivity.this,InformationActivity.class);
                startActivity(infoIntent);
            }
        });
        //拍摄
        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(cameraPermissions, REQUEST_CAMERA_PERMISSIONS);
                    }
                }
                else {
                    startActivity(new Intent(MainActivity.this, RecordActivity.class));
                }
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    //初始化视频列表
    private void initRecyclerView() {
        videoList = findViewById(R.id.videoList);
        videoList.setLayoutManager(new LinearLayoutManager(this));
        videoList.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                ImageView imageView = new ImageView(viewGroup.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                return new MainActivity.MyViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
                ImageView iv = (ImageView) viewHolder.itemView;
                String url = mFeeds.get(i).getImage_url();
                //将封面图放入每一个item
                Glide.with(iv.getContext())
                      .applyDefaultRequestOptions(options)
                      .load(url)
                      .into(iv);
                //点击封面图去播放相应视频
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String videoUrl=mFeeds.get(i).getVideo_url();
                        if(videoUrl==null){
                            Toast.makeText(MainActivity.this,"视频不存在，请重试！",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Intent playIntent = new Intent(MainActivity.this, VideoPlayActivity.class);
                            playIntent.putExtra("videoUrl", videoUrl);
                            startActivity(playIntent);
                        }
                    }
                });
            }

            @Override public int getItemCount() {
                return mFeeds.size();
            }
        });
    }

    //从网络获取视频列表
    public void fetchFeed() {
                 retrofit= new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IMiniDouyinService iMiniDouyinService=retrofit.create(IMiniDouyinService.class);
        feedResponseCall=iMiniDouyinService.fetchFeed();
        feedResponseCall.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                if(response.isSuccessful()){
                    mFeeds= response.body().getFeeds();
                    Log.d("help:", "视频数目: "+mFeeds.size());
                    videoList.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,"请求超时！",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_NETWORK: {
                //判断权限是否已经授予
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    fetchFeed();
                } else {
                    Toast.makeText(this, "权限被禁用", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_CAMERA_PERMISSIONS:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                  && grantResults[2]==PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(MainActivity.this, RecordActivity.class));
                }
                else{
                    Toast.makeText(this, "权限被禁用", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

}