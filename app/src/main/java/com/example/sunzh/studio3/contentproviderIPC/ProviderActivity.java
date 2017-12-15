package com.example.sunzh.studio3.contentproviderIPC;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.sunzh.studio3.R;
import com.example.sunzh.studio3.contentproviderIPC.db.DbOpenHelper;

import static com.example.sunzh.studio3.contentproviderIPC.BookProvider.AUTHORITY;

public class ProviderActivity extends AppCompatActivity {

    private static final String TAG = ProviderActivity.class.getSimpleName();
    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
        }
    };
    private ContentResolver resolver;
    ContentObserver observer = new ContentObserver(mainHandler) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        Uri uri = Uri.parse("content://com.sunzf.bookprovider/user");
        resolver = getContentResolver();
        resolver.registerContentObserver(uri, false, observer);
//        resolver.query(uri, null, null, null, null);
//        resolver.query(uri, null, null, null, null);
//        resolver.query(uri, null, null, null, null);
        testPorvider();
    }

    private void testPorvider() {


        Uri bookUri = Uri.parse("content://" + AUTHORITY + "/book");
        ContentValues values = new ContentValues();
        values.put(DbOpenHelper.BOOK_COLUMN_ID, 6);
        values.put(DbOpenHelper.BOOK_COLUMN_NAME, "Python");
        resolver.insert(bookUri, values);
        Cursor bookCursor = resolver.query(bookUri, new String[]{DbOpenHelper.BOOK_COLUMN_ID, DbOpenHelper.BOOK_COLUMN_NAME}, null, null, null);
        while (bookCursor.moveToNext()) {
            StringBuilder builder = new StringBuilder();
            int id = bookCursor.getInt(0);
            String name = bookCursor.getString(1);
            builder.append("id: " + id);
            builder.append(", name: " + name);
            Log.d(TAG, builder.toString());
        }
        bookCursor.close();

        Uri userUri = Uri.parse("content://" + AUTHORITY + "/user");
        Cursor query = resolver.query(userUri, new String[]{DbOpenHelper.USER_COLUMN_ID, DbOpenHelper.USER_COLUMN_NAME, DbOpenHelper.USER_COLUMN_SEX}, null, null, null);
        while (query.moveToNext()) {
            StringBuilder builder = new StringBuilder();
            int id = query.getInt(0);
            String name = query.getString(1);
            int sex = query.getInt(2);
            builder.append("id: " + id);
            builder.append(", name: " + name);
            builder.append(", sex: " + (sex == 0 ? "女" : "男"));
            Log.d(TAG, builder.toString());
        }
        query.close();
    }


    @Override
    protected void onDestroy() {
        resolver.unregisterContentObserver(observer);
        super.onDestroy();
    }
}
