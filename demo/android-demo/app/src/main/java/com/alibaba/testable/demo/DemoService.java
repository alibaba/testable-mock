package com.alibaba.testable.demo;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

/**
 * 演示与Robolectric配合使用
 * Demonstrate using mock along with Robolectric
 */
public class DemoService extends Service {

    static final String TAG = "DemoService";
    SharedPreferences mPreference;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && "start_foreground".equalsIgnoreCase(intent.getAction())) {
            Log.d(TAG, "start service.");
        } else if(intent != null && "stop_foreground".equalsIgnoreCase(intent.getAction())) {
            Log.d(TAG, "stop service.");
        }
        return START_REDELIVER_INTENT;
    }
}
