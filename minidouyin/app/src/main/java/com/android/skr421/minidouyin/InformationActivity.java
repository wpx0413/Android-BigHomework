package com.android.skr421.minidouyin;



import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.skr421.minidouyin.db.InfoContract;
import com.android.skr421.minidouyin.db.InfoDbHelper;



public class InformationActivity extends AppCompatActivity {

    private EditText studentEdt;
    private EditText userNameEdt;
    private Button save_Btn;
    private InfoDbHelper infoDbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private String studentId="";
    private String userName="";
    private String studentText;
    private String usernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        studentEdt=findViewById(R.id.student_id_edt);
        userNameEdt=findViewById(R.id.user_name_edt);
        save_Btn=findViewById(R.id.save_btn);
        save_Btn.setBackgroundColor(Color.BLACK);
        save_Btn.setTextColor(Color.WHITE);
        save_Btn.setAlpha(0.7f);

        infoDbHelper=new InfoDbHelper(this);
        sqLiteDatabase=infoDbHelper.getWritableDatabase();
        getDataFromDatabase();
        studentEdt.setText(studentId);
        userNameEdt.setText(userName);
        //保存个人信息
        save_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                studentText=studentEdt.getText().toString();
                usernameText=userNameEdt.getText().toString();
                if("".equals(studentText) || "".equals(usernameText)){
                    Toast.makeText(InformationActivity.this,"信息不能为空",Toast.LENGTH_SHORT).show();
                }
                else{
                    if("".equals(studentId)){
                        if(saveInfoDatabase()){
                            Toast.makeText(InformationActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent();
                            intent.putExtra("isSetting",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                        else{
                            Toast.makeText(InformationActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent();
                            intent.putExtra("isSetting",false);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    else{
                        if(updateInfo()){
                            Toast.makeText(InformationActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(InformationActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });
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

    private boolean updateInfo() {
        // 更新数据
        if(sqLiteDatabase==null){
            return false;
        }
        ContentValues values=new ContentValues();
        values.put(InfoContract.InfoEntry.COLUMN_STUDENT_ID,studentText);
        values.put(InfoContract.InfoEntry.COLUMN_USERNAME,usernameText);
        String selection= InfoContract.InfoEntry.COLUMN_STUDENT_ID +" LIKE ?";
        String[] selectArgs={studentId};
        int count=sqLiteDatabase.update(InfoContract.InfoEntry.TABLE_NAME,values,selection,selectArgs);
        if(count>0){
            return true;
        }
        return false;
    }

    private boolean saveInfoDatabase() {
        // 插入数据
        if (sqLiteDatabase == null ) {
            return false;
        }

        ContentValues values=new ContentValues();
        values.put(InfoContract.InfoEntry.COLUMN_STUDENT_ID,studentText);
        values.put(InfoContract.InfoEntry.COLUMN_USERNAME, usernameText);
        long newRowId=sqLiteDatabase.insert(InfoContract.InfoEntry.TABLE_NAME,null,values);
        if(newRowId!=-1){
            return true;
        }
        else{
            return false;
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
