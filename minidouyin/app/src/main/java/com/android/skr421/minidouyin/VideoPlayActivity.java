package com.android.skr421.minidouyin;



import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.skr421.minidouyin.myview.MyVideoView;

import java.util.concurrent.TimeUnit;

public class VideoPlayActivity extends AppCompatActivity {

    private MyVideoView videoView;
    private Uri videoUri;
    private Button playPause_btn;
    private long mCurTime;
    private long mLastTime;
    private ImageView heartView;
    private LottieAnimationView lordLottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mCurTime=mLastTime=System.currentTimeMillis();
        videoView=findViewById(R.id.videoView);
        playPause_btn=findViewById(R.id.play_pause_btn);
        playPause_btn.setBackgroundColor(Color.BLACK);
        playPause_btn.setTextColor(Color.WHITE);
        heartView=findViewById(R.id.heartView);
        heartView.setAlpha(0f);
        lordLottie=findViewById(R.id.lord_lottie);
        lordLottie.bringToFront();
        //绑定视频uri
        Intent intent=getIntent();
        String videoUrl=intent.getStringExtra("videoUrl");
        videoUri=Uri.parse(videoUrl);
        Log.d("Uri", "onCreate:"+videoUri);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();
        playPause_btn.setText(R.string.pause);
        //加载完成时隐藏加载动画
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                lordLottie.setVisibility(View.GONE);
            }
        });
        //完成事件
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playPause_btn.setText(R.string.play);
            }
        });
        //点击视频窗口继续、暂停
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoView.isPlaying())
                {
                    videoView.pause();
                    playPause_btn.setText(R.string.play);
                }
                else{
                    videoView.start();
                    playPause_btn.setText(R.string.pause);
                }
                mLastTime = mCurTime;
                mCurTime = System.currentTimeMillis();
                if (mCurTime - mLastTime < 500) {//双击点赞
                    Log.d("help", "onClick: 双击点赞");
                    heartView.bringToFront();
                    ObjectAnimator heartAnimator = ObjectAnimator.ofFloat(heartView, "alpha", 0f, 1f);
                    heartAnimator.setInterpolator(new LinearInterpolator());
                    heartAnimator.setDuration(300);
                    heartAnimator.start();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(heartView, "alpha", 1f, 0f);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(1300);
                    animator.start();
                }

            }
        });
        //开始和暂停按钮
        playPause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoView.isPlaying())
                {
                    videoView.pause();
                    playPause_btn.setText(R.string.play);
                }
                else{
                    videoView.start();
                    playPause_btn.setText(R.string.pause);
                }
            }
        });
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        videoView.start();
        playPause_btn.setText(R.string.pause);
    }
}