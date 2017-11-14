package com.example.sunzh.studio3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remoteservice.Service.IStudentService;
import com.example.remoteservice.Service.Student;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * 连接服务的对象
     */
    private ServiceConnection conn;
    /**
     * 服务器端返回的对象
     */
    private IStudentService iStudentService;
    private TextView etAidlId;
    /**
     * 当Binder死亡时，我们就会通过这个代理收到通知这时我们可以重新发起请求连接
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if(iStudentService == null) {
                return;
            }
            iStudentService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            iStudentService = null;
            //此下面就是重新建立进程间通信
            bindService(intent, conn, BIND_AUTO_CREATE);
        }
    };
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etAidlId = (TextView) findViewById(R.id.et_aidl_id);
    }

    /**
     * 绑定远程服务
     *
     * @param view
     */
    public void bindRemoteService(View view) {
        intent = createIntent(this, new Intent("com.example.remoteservice.Service.MyStudentService"));
        if (intent != null && conn == null) {
            conn = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    iStudentService = IStudentService.Stub.asInterface(service);
                    try {
                        service.linkToDeath(mDeathRecipient, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Toast.makeText(MainActivity.this, "绑定服务", Toast.LENGTH_SHORT).show();
    }

    private Intent createIntent(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);
        if (resolveInfos == null || resolveInfos.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfos.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        Intent intent1 = new Intent(intent);
        intent1.setComponent(new ComponentName(packageName, className));
        return intent1;
    }

    /**
     * 调用远程服务的方法
     *
     * @param view
     */
    public void invokeRemote(View view) {
        String idStr = etAidlId.getText().toString();
        if (!TextUtils.isEmpty(idStr)) {
            int id = Integer.parseInt(idStr);
            try {
                Student student = iStudentService.getStudentById(id);
                Log.e("TAG", "" + student);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void setRequestedOrientation(int requestedOrientation) {
//        return;
//    }

    /**
     * 解绑远程服务
     *
     * @param view
     */
    public void unbindRemoteService(View view) {
        if (conn != null) {
            unbindService(conn);
            conn = null;
            Toast.makeText(MainActivity.this, "解绑服务", Toast.LENGTH_SHORT).show();
        }
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}
