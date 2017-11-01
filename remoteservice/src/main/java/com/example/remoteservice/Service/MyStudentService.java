package com.example.remoteservice.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MyStudentService extends Service {

    public static final String TAG = "服务器服务";

    public MyStudentService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new StudentService();
    }

    private class StudentService extends IStudentService.Stub {

        @Override
        public Student getStudentById(int id) throws RemoteException {
            //这里就是返回数据的逻辑
            return new Student(1001, "Lucy", 12);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
    }
}
