package com.example.sunzh.studio3.local;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsbridgedemo.H5Activity;
import com.example.remoteservice.Book;
import com.example.remoteservice.IBookManager;
import com.example.remoteservice.IOnNewBookArrivedListener;
import com.example.remoteservice.Service.IStudentService;
import com.example.remoteservice.Service.Student;
import com.example.sunzh.studio3.R;
import com.example.sunzh.studio3.contentproviderIPC.ProviderActivity;
import com.example.sunzh.studio3.remote.BookManagerService;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int MSG_NEW_BOOK_ARRIVED = 1;
    private static final int NOTIFICATION_REQUESTCODE = 1;
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 101;
    /**
     * private static final int NOTIFICATION_FLAG = 1;
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
            if (iStudentService == null) {
                return;
            }
            iStudentService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            iStudentService = null;
            //此下面就是重新建立进程间通信
            bindService(intent, conn, BIND_AUTO_CREATE);
        }
    };
    private Intent intent;
    private android.os.Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "recieve new book: " + msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MSG_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };
    private ServiceConnection mBookConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = IBookManager.Stub.asInterface(service);

            try {
                List<Book> list = bookManager.getBookList();
                Log.i(TAG, "query book list: " + list.toString());
                Book newBook = new Book(3, "开发艺术探索");
                bookManager.addBook(newBook);
                Log.i(TAG, "add book: " + newBook);
                List<Book> newList = bookManager.getBookList();
                Log.i(TAG, "query book list: " + newList.toString());
                bookManager.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private IBookManager bookManager;
    private int test = 0;


    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, BService.class));
        builder = new NotificationCompat.Builder(this.getApplicationContext(), "test");
        notificationManager = (NotificationManager) this.getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        etAidlId = (TextView) findViewById(R.id.et_aidl_id);
        bindService(new Intent(this, BookManagerService.class), mBookConn, BIND_AUTO_CREATE);
        findViewById(R.id.requestPermission).setOnClickListener(this);
        findViewById(R.id.btn_content_provider).setOnClickListener(this);
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
        int integer = getResources().getInteger(R.integer.int1);
        Log.e("INTEGER", "integer=" + integer);
//        getResources().getFraction(R.fraction.fraction1,1,1);
        getResources().getFraction(R.fraction.ss, 11, 11);
    }

    @Override
    protected void onDestroy() {
        if (mBookConn != null && bookManager.asBinder().isBinderAlive()) {
            try {
                bookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(mBookConn);
        }
        if (conn != null) {
            unbindService(conn);
        }
        super.onDestroy();
    }

    public void jsBridge(View view) {
        H5Activity.openH5(this, "http://www.baidu.com");
    }

    public void notify(View view) {

        startService(new Intent(this, BService.class));
//        notifi();
    }

    private void notifi() {
        RemoteViews remoteview = new RemoteViews(getPackageName(), R.layout.remoteview);
//        remoteview.setOnClickFillInIntent(R.id.img_header, );


        test++;
        builder.setSmallIcon(android.R.drawable.sym_action_chat);//icon图标，如果不设置，Notification不会显示出来
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.sym_action_call));
        builder.setTicker("滚动提示,滚动提示,滚动提示\n滚动提示,滚动提示,滚动提示.....");//滚动提示
        builder.setContentTitle("这个是标题" + test);//标题
        builder.setContentText("这个是内容" + test + "\r\n内空副！！！").setNumber(test);//内容
        builder.setContentInfo("右侧提示");
        builder.setNumber(test);//在右边显示一个数量，等价于setcontentinfo函数，如果有设置setcontentinfo，那么本函数会被覆盖
        builder.setOngoing(true);//是否常驻状态栏
        builder.setOnlyAlertOnce(true);//是否只提示一次，true-如果notification已经存在状态栏即使再调用notify也不会更新
        builder.setProgress(100, 50, false);//滚动条。第三个参数：true-不确定的，不会显示进度条，false-根据max和progress来显示进度条
        builder.setUsesChronometer(true);//显示一个计数器
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//            inboxStyle.setBigContentTitle("boxstyle:");
//            inboxStyle.addLine("1");
//            inboxStyle.addLine("2");
//            builder.setStyle(inboxStyle);
//        }
//        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        //铃声，震动，提示灯默认设置
        //铃声、振动、提示灯设置，DEFAULT_ALL会忽略已经设置的所有效果
//        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring));
        builder.setLights(Color.BLUE, 500, 500);
//        builder.setSound(Uri.parse("file///android_asset/music/ring.wav"));
//        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);
//        builder.setPriority(Notification.PRIORITY_HIGH);


        //意图
        Intent intent1 = new Intent(this, H5Activity.class);
        intent1.putExtra(H5Activity.H5_URL, "https://github.com/");

        //生成pendingintent的两种方法
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(intent1);
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        PendingIntent broadcast = PendingIntent.getBroadcast(this, REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setContent(remoteview);


        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(test, notification);
    }

    /**
     * 请求悬浮窗权限
     */
    private void requestAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                Toast.makeText(MainActivity.this, "已申请", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "android6.0系统以下不需要申请", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 请求更改系统设置权限
     */
    private void requestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
            } else {
                Toast.makeText(MainActivity.this, "已申请", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "android6.0系统以下不需要申请", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Log.i(TAG, "悬浮窗权限已允许");
                    Toast.makeText(MainActivity.this, "悬浮窗权限已允许", Toast.LENGTH_SHORT).show();
                    if (!Settings.System.canWrite(this)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SystemClock.sleep(3000);
                                requestWriteSettings();
                            }
                        }).start();
                    }
                }
            }
        }
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    Log.i(TAG, "更改系统设置权限已允许");
                    Toast.makeText(MainActivity.this, "更改系统设置权限已允许", Toast.LENGTH_SHORT).show();
                }
            }
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.requestPermission:
                requestAlertWindowPermission();
                break;
            case R.id.btn_content_provider:
                startActivity(new Intent(this, ProviderActivity.class));
                break;
            default:
                break;
        }
    }
}
