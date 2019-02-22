package com.bingor.camerahelperlib;

import android.app.Application;

import com.bingor.utillib.log.Log;

/**
 * Created by Bingor on 2019/2/14.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.TAG = "HXB";
    }
}
