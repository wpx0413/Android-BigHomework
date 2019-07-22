package com.android.skr421.minidouyin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RecordActivity extends AppCompatActivity {

    private Button record_btn;
    private Button face_btn;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private File videoFile;
    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;
    private int rotationDegree = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record);
        mSurfaceView = findViewById(R.id.img);
        initBtn();
        //给SurfaceHolder添加Callback
        final SurfaceHolder surfaceHolder=mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mCamera = getCamera(CAMERA_TYPE);//打开后摄
                startPreview(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Camera.Parameters parameters = mCamera.getParameters();
                size = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), i1, i2);
                parameters.setPreviewSize(size.width, size.height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCameraAndPreview();
            }
        });


        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //录制
                if (isRecording) {
                    // 停止录制
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    //通知图库刷新列表
                    RecordActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(videoFile.getPath()))));
                    mCamera.lock();
                    isRecording = false;
                    Toast.makeText(RecordActivity.this,"录制完成",Toast.LENGTH_SHORT).show();
                    record_btn.setBackgroundColor(Color.BLACK);
                    record_btn.setText(R.string.begin_record);
                } else {
                    // 录制
                    mCamera.unlock();
                    if (prepareVideoRecorder()) {
                        try {
                            mMediaRecorder.prepare();
                            mMediaRecorder.start();
                            isRecording = true;
                            Toast.makeText(RecordActivity.this,"开始录制",Toast.LENGTH_SHORT).show();
                            record_btn.setBackgroundColor(Color.RED);
                            record_btn.setText(getString(R.string.stop_record));
                        } catch (Exception e) {
                            releaseMediaRecorder();
                        }
                    }
                }
            }
        });



        face_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //切换前后摄像头
                if(CAMERA_TYPE== Camera.CameraInfo.CAMERA_FACING_BACK){
                    CAMERA_TYPE= Camera.CameraInfo.CAMERA_FACING_FRONT;
                }
                else
                {
                    CAMERA_TYPE=Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                mCamera=getCamera(CAMERA_TYPE);
                startPreview(surfaceHolder);
            }
        });
    }


    //初始化按钮
    private void initBtn()
    {
        record_btn=findViewById(R.id.btn_record);
        face_btn=findViewById(R.id.btn_facing);
        record_btn.setBackgroundColor(Color.BLACK);
        face_btn.setBackgroundColor(Color.BLACK);
        record_btn.setTextColor(Color.WHITE);
        face_btn.setTextColor(Color.WHITE);
        record_btn.setAlpha(0.5f);
        face_btn.setAlpha(0.5f);
    }



    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        // 设置旋转方向
        rotationDegree=getCameraDisplayOrientation(CAMERA_TYPE);
        cam.setDisplayOrientation(rotationDegree);
        //设置尺寸
        if(CAMERA_TYPE==Camera.CameraInfo.CAMERA_FACING_BACK) {
            try {
                Camera.Parameters parameters = cam.getParameters();
                Point bestPreviewSizeValue1 = findBestPreviewSizeValue(parameters.getSupportedPreviewSizes());
                parameters.setPreviewSize((int)(bestPreviewSizeValue1.x*0.99), bestPreviewSizeValue1.y);
                cam.setParameters(parameters);
            } catch (Exception e) {
                Log.d("help", "back camera set parameters fail");
            }
        }
        /*else if(CAMERA_TYPE==Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            try {
                Camera.Parameters parameters = cam.getParameters();
                Point bestPreviewSizeValue1 = findBestPreviewSizeValue(parameters.getSupportedPreviewSizes());
                parameters.setPreviewSize((int)(bestPreviewSizeValue1.x*0.98), bestPreviewSizeValue1.y);
                cam.setParameters(parameters);
            } catch (Exception e) {
                Log.d("help", "front camera set parameters fail");
            }
        }*/
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //释放camera资源
        mCamera.stopPreview();
        mCamera.release();
        mCamera=null;
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //开始预览
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }


    private boolean prepareVideoRecorder() {
        // 准备MediaRecorder
        if(mMediaRecorder!=null){
            return false;
        }
        mMediaRecorder=new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);
        //处理拍摄视频的旋转角度
        if(CAMERA_TYPE==Camera.CameraInfo.CAMERA_FACING_BACK){
            mMediaRecorder.setOrientationHint(rotationDegree);
        }
        else{
            mMediaRecorder.setOrientationHint(360-rotationDegree);
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        videoFile=getOutputMediaFile();
        mMediaRecorder.setOutputFile(videoFile.toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        return true;
    }


    private void releaseMediaRecorder() {
        // 释放MediaRecorder
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder=null;
    }


    private static Point findBestPreviewSizeValue(List<Camera.Size> sizeList){
        int bestX = 0;
        int bestY = 0;
        int size = 0;
        for (Camera.Size nowSize : sizeList){
            int newX = nowSize.width;
            int newY = nowSize.height;
            int newSize = Math.abs(newX * newX) + Math.abs(newY * newY);
            float ratio = (float) (newY * 1.0 / newX);
            if(newSize >= size && ratio != 0.75){//确保图片是16:9
                bestX  = newX;
                bestY = newY;
                size = newSize;
            }else if(newSize < size){
                continue;
            }
        }
        if(bestX > 0 && bestY > 0){
            return new Point(bestX,bestY);
        }
        return null;

    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    //在公共文件夹创建一个文件
    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MiniDouYin");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        Log.d("myfile", "getOutputMediaFile: "+mediaFile);
        return mediaFile;
    }

}
