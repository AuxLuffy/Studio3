package com.example.sunzh.studio3.local;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.example.jsbridgedemo.H5Activity;
import com.example.sunzh.studio3.R;

public class BService extends Service {
    public static final String MUSIC_MAIN_ACTION = "music_main_action";
    private static final int REQUEST_CODE_PLAY = 1001;
    private static final int REQUEST_CODE_CLOSE = 1002;
    private static final int REQUEST_CODE_NEXT = 1003;
    public static final String CONTROL_TAG = "control";
    private static final String CONTROL_PLAY = "play_music";
    private static final String CONTROL_CLOSE = "close_music";
    private static final String CONTROL_NEXT = "next_music";

    private int test;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private MusicServiceBroadcastReceiver musicBroadCast;
    private PendingIntent pplay, pclose, pnext;
    private Intent play, close, next;

    public BService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        builder = new NotificationCompat.Builder(getApplicationContext(), "test");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notific();
        musicBroadCast = new MusicServiceBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_MAIN_ACTION);
        registerReceiver(musicBroadCast, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void notific() {


        RemoteViews remoteview = new RemoteViews(getPackageName(), R.layout.remoteview);
//        remoteview.setOnClickFillInIntent(R.id.img_header, );
        
        //注册播放事件
        play = new Intent(MUSIC_MAIN_ACTION);
        play.putExtra(CONTROL_TAG, CONTROL_PLAY);
        pplay = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE_PLAY, play, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteview.setOnClickPendingIntent(R.id.iv_nofitication_kzhi_play, pplay);
        
        close = new Intent(MUSIC_MAIN_ACTION);
        close.putExtra(CONTROL_TAG, CONTROL_CLOSE);
        pclose = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE_CLOSE, close, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteview.setOnClickPendingIntent(R.id.iv_nofitication_kzhi_close, pclose);
        
        next = new Intent(MUSIC_MAIN_ACTION);
        next.putExtra(CONTROL_TAG, CONTROL_NEXT);
        pnext = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE_NEXT, next, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteview.setOnClickPendingIntent(R.id.iv_nofitication_kzhi_next, pnext);
        

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

        PendingIntent broadcast = PendingIntent.getBroadcast(this, 1001, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setContent(remoteview);

        Notification notification = null;
        if (Build.VERSION_CODES.JELLY_BEAN < Build.VERSION.SDK_INT) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(test, notification);
    }

}
