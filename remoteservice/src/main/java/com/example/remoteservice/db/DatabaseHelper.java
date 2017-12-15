package com.example.remoteservice.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.remoteservice.MainActivity;

/**
 * Created by sunzh on 2017/12/15.
 *
 * @author sunzh
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "book_provider.db";//数据库名称
    private static final int version = 1;//数据库版本

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(MainActivity context) {
        this(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
