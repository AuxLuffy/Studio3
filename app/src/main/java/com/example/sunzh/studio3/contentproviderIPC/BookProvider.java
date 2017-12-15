package com.example.sunzh.studio3.contentproviderIPC;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.sunzh.studio3.contentproviderIPC.db.DbOpenHelper;

public class BookProvider extends ContentProvider {

    private static final String TAG = BookProvider.class.getSimpleName();
    public static final String AUTHORITY = "com.sunzf.bookprovider";
    public static final Uri BOOK_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/book");
    public static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

    public static final int BOOK_URI_CODE = 0;
    public static final int USER_URI_CODE = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "book", BOOK_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "user", USER_URI_CODE);
    }

    private Context context;
    private SQLiteDatabase db;


    public BookProvider() {
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Log.d(TAG, "onCreate, current thread:" + Thread.currentThread().getName());//运行在主线程，所以不能在这个里面做耗时操作
        context = getContext();
        initProviderData();
        return true;
    }

    /**
     * contentProvider创建时初始化数据库。注意，实际场景中可能会有耗时操作，此方法最好在子线程中操作
     */
    private void initProviderData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                DbOpenHelper dbOpenHelper = new DbOpenHelper(context);

                db = dbOpenHelper.getWritableDatabase();
//                db.beginTransactionNonExclusive();
                db.execSQL("delete from " + DbOpenHelper.BOOK_TABLE_NAME);
                db.execSQL("delete from " + DbOpenHelper.USER_TABLE_NAME);
                db.execSQL("insert into book values(3,'Android');");
                db.execSQL("insert into book values(4,'IOS');");
                db.execSQL("insert into book values(5,'html');");
                db.execSQL("insert into user values(1,'jack',1);");
                db.execSQL("insert into user values(7,'Mary',0);");
//            }
//        }).start();

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Log.d(TAG, "query, current thread: " + Thread.currentThread().getName());//运行在binder线程池中，每次操作可能运行线程不同
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        Log.d(TAG, "insert");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        db.insert(table, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        Log.d(TAG, "update");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int row = db.update(table, values, selection, selectionArgs);
        if (row > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        Log.d(TAG, "delete");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int count = db.delete(table, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }


    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        Log.d(TAG, "getType");
        return null;
    }

    private String getTableName(Uri uri) {
        String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case BOOK_URI_CODE:
                tableName = DbOpenHelper.BOOK_TABLE_NAME;
                break;
            case USER_URI_CODE:
                tableName = DbOpenHelper.USER_TABLE_NAME;
                break;
        }
        return tableName;
    }

}
