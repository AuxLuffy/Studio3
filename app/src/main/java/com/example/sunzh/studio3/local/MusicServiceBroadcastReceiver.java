package com.example.sunzh.studio3.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TAG", "收到广播" + intent.getStringExtra(BService.CONTROL_TAG));
    }
}
