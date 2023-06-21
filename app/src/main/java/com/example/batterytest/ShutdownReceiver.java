package com.example.batterytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                // 在此处执行关机事件触发后的操作
                // 例如保存数据、释放资源等

            }
        }
    }