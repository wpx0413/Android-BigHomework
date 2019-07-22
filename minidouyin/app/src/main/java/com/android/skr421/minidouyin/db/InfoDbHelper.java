package com.android.skr421.minidouyin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class InfoDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String DATABASE_NAME = "info.db";

    public InfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(InfoContract.SQL_CREATE_TABLE);
        Log.d("help", "onCreate: 建表");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
