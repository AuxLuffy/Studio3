package com.example.sunzh.studio3.local;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String control = intent.getStringExtra(BService.CONTROL_TAG);
        if (BService.CONTROL_CLOSE.equals(control)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(11);
        }
        Log.i("TAG", "收到广播" + control);
    }
}
