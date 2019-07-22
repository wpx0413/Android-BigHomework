package com.android.skr421.minidouyin.myview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.android.skr421.minidouyin.utils.SSlUtils;

import javax.net.ssl.HttpsURLConnection;

public class MyVideoView extends VideoView {

    public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context) {
        super(context);
    }

    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSlUtils.createSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new SSlUtils.TrustAllHostnameVerifier());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
