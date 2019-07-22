package com.android.skr421.minidouyin.db;

import android.provider.BaseColumns;

public class InfoContract {

    private InfoContract() {
    }

    public static class InfoEntry implements BaseColumns {
        public static final String TABLE_NAME="Info";
        public static final String COLUMN_STUDENT_ID="student_id";
        public static final String COLUMN_USERNAME="user_name";
    }

    //建表语句
    public static final String SQL_CREATE_TABLE=
            "CREATE TABLE "+InfoEntry.TABLE_NAME+" ("+
                    InfoEntry.COLUMN_STUDENT_ID+" TEXT PRIMARY KEY, "+
                    InfoEntry.COLUMN_USERNAME+" TEXT "+")";
}
