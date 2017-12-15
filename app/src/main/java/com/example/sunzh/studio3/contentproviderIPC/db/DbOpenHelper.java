package com.example.sunzh.studio3.contentproviderIPC.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by sunzh on 2017/12/15.
 *
 * @author sunzh
 */

public class DbOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = DbOpenHelper.class.getSimpleName();
    private static final String DB_NAME = "book_provider.db";//可以不必指定后缀名
    private static final int DB_VERSION = 1;

    public static final String BOOK_TABLE_NAME = "book";
    public static final String BOOK_COLUMN_ID = "_id";
    public static final String BOOK_COLUMN_NAME = "name";
    public static final String USER_TABLE_NAME = "user";
    public static final String USER_COLUMN_ID = "_id";
    public static final String USER_COLUMN_NAME = "name";
    public static final String USER_COLUMN_SEX = "sex";

    //SQL对大小写不敏感
    private String CREATE_BOOK_TABLE = "CREATE TABLE IF NOT EXISTS "
            + BOOK_TABLE_NAME + "("
            + BOOK_COLUMN_ID + " INTEGER not null PRIMARY KEY autoincrement,"
            + BOOK_COLUMN_NAME + " TEXT)";
    private String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS "
            + USER_TABLE_NAME + "("
            + USER_COLUMN_ID + " INTEGER not null PRIMARY KEY autoincrement,"
            + USER_COLUMN_NAME + " TEXT,"
            + USER_COLUMN_SEX + " INT)";

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade");
    }
}
