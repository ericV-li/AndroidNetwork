package com.eric.networkdemo;

import android.app.Application;

import com.eric.core.core.HttpClientManager;

/**
 * @author li
 * @Package com.eric.networkdemo
 * @Title: MyApplication
 * @Description: Copyright (c)
 * Create DateTime: 2018/10/26
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HttpClientManager.getInstance().init(getApplicationContext(),true, 10000, 10000, true,true);
    }
}
