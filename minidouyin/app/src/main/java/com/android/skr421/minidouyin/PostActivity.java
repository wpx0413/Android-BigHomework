package com.android.skr421.minidouyin;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.skr421.minidouyin.bean.PostVideoResponse;
import com.android.skr421.minidouyin.db.InfoContract;
import com.android.skr421.minidouyin.db.InfoDbHelper;
import com.android.skr421.minidouyin.newtork.IMiniDouyinService;
import com.android.skr421.minidouyin.utils.ResourceUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostActivity extends AppCompatActivity {

    private Button button;
    private Uri imageUri;
    private Uri videoUri;
    private String studentId="";
    private String userName="";
    private InfoDbHelper infoDbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private static final int PICK_IMAGE=101;
    private static final int PICK_VIDEO=102;
    private static final int REQUEST_PERMISSION=1;
    private static final int REQUEST_SET_INFO=103;
    private static String[] mPermissions=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initButton();
        infoDbHelper=new InfoDbHelper(this);
        sqLiteDatabase=infoDbHelper.getWritableDatabase();
        getDataFromDatabase();
    }

    //初始化按钮
    private void initButton()
    {
        button=findViewById(R.id.selectRes);
        button.setBackgroundColor(Color.BLACK);
        button.setAlpha(0.7f);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查权限
                if(ContextCompat.checkSelfPermission(PostActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(PostActivity.this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//安卓版本小于6.0则不用
                        requestPermissions(mPermissions,REQUEST_PERMISSION);
                    }
                }
                //根据按钮信息决定选图，选视频还是上传
                else {
                    String s = button.getText().toString();
                    if (getString(R.string.select_image).equals(s)) {
                        chooseImage();
                    } else if (getString(R.string.select_video).equals(s)) {
                        chooseVideo();
                    } else {//准备上传
                        if (imageUri != null && videoUri != null) {
                            if(checkInfo()){
                                postVideo();
                            }
                            else{
                                Intent intent=new Intent(PostActivity.this,InformationActivity.class);
                                Toast.makeText(PostActivity.this,"设置用户信息后才能发布作品哦！",Toast.LENGTH_LONG).show();
                                startActivityForResult(intent,REQUEST_SET_INFO);
                            }
                        } else {
                            throw new IllegalArgumentException("error data uri, imageUri = " + imageUri + ", videoUri = " + videoUri);
                        }
                    }
                }
            }
        });
    }

    //选择封面
    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    //选择视频
    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    //检查是否已设置用户名
    private boolean checkInfo(){
        if("".equals(userName) || "".equals(studentId)){
            return false;
        }
        else{
            return true;
        }
    }


    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(PostActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    //上传
    private void postVideo() {
        button.setText("正在上传...");
        button.setEnabled(false);
        OkHttpClient.Builder httpBuilder=new OkHttpClient.Builder();
        OkHttpClient client=httpBuilder.readTimeout(2, TimeUnit.SECONDS)
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS )//设置超时
                .build();
        Retrofit retrofit= new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IMiniDouyinService iMiniDouyinService=retrofit.create(IMiniDouyinService.class);
        Call<PostVideoResponse> postVideoResponseCall=iMiniDouyinService.createVideo(studentId,userName,
                getMultipartFromUri("cover_image",imageUri),getMultipartFromUri("video",videoUri));
        Log.d("url", "postVideo: "+getMultipartFromUri("cover_image",imageUri));
        Log.d("url", "postVideo: "+getMultipartFromUri("video",videoUri));
        postVideoResponseCall.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                Toast.makeText(PostActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                button.setText(getString(R.string.select_image));
                button.setEnabled(true);
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                Toast.makeText(PostActivity.this,"上传成功！",Toast.LENGTH_SHORT).show();
                Log.d("help", "onFailure: "+t.toString());
                button.setText(getString(R.string.select_image));
                button.setEnabled(true);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                imageUri = data.getData();
                Log.d("help", "selectedImage = " + imageUri);
                button.setText(R.string.select_video);
            } else if (requestCode == PICK_VIDEO) {
                videoUri = data.getData();
                Log.d("help", "mSelectedVideo = " +videoUri);
                button.setText(R.string.post);
            }else if(requestCode == REQUEST_SET_INFO){
                boolean isSetting =data.getBooleanExtra("isSetting",false);
                if(isSetting){//若信息已设置，重新读取信息
                    getDataFromDatabase();
                }
            }
        }
    }




    private void getDataFromDatabase() {
        if(sqLiteDatabase==null){
            return ;
        }
        Cursor cursor=null;
        try{
            cursor=sqLiteDatabase.query(InfoContract.InfoEntry.TABLE_NAME,null,null,null,null,null,null);
            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                studentId=cursor.getString(cursor.getColumnIndex(InfoContract.InfoEntry.COLUMN_STUDENT_ID));
                userName=cursor.getString(cursor.getColumnIndex(InfoContract.InfoEntry.COLUMN_USERNAME));
            }
        }
        finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        infoDbHelper.close();
        infoDbHelper=null;
        sqLiteDatabase.close();
        sqLiteDatabase=null;
    }


}
