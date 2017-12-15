package com.example.remoteservice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.remoteservice.db.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DANGEROUS_ACTION = "sunzf.intent.action.DANGEROUS_TEST";

    private static final String NORMAL_ACTION = "sunzf.intent.action.NORMAL_TEST";

    private static final String DANGEROUS_PERMISSION = "sunzf.permission.DANGEROUS_TEST";

    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.dangerous_perm).setOnClickListener(this);
        findViewById(R.id.normal_perm).setOnClickListener(this);
        findViewById(R.id.request).setOnClickListener(this);
        mTv = findViewById(R.id.tv_info);

        new DatabaseHelper(this);
        SQLiteDatabase db ;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dangerous_perm:
                try {
                    startActivity(new Intent(DANGEROUS_ACTION));
                    mTv.setText("自定义权限测试");
                } catch (Exception e) {
                    e.printStackTrace();
                    mTv.setText(e.getMessage());
                }
                break;
            case R.id.normal_perm:
                try {
                    startActivity(new Intent(NORMAL_ACTION));
                    mTv.setText("自定义权限测试");
                } catch (Exception e) {
                    e.printStackTrace();
                    mTv.setText(e.getMessage());
                }
                break;
            case R.id.request:
                try {
                    if (ActivityCompat.checkSelfPermission(this, DANGEROUS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{DANGEROUS_PERMISSION}, 1);
                    } else {
                        startActivity(new Intent(DANGEROUS_ACTION));
                    }
                    mTv.setText("自定义权限测试");
                } catch (Exception e) {
                    e.printStackTrace();
                    mTv.setText(e.getMessage());
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length > 0) {
            switch (requestCode) {
                case 1:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(DANGEROUS_ACTION));
                    }
                    break;
            }
        }
    }
}
