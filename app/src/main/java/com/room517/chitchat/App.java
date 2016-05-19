package com.room517.chitchat;

import android.app.Application;

import io.rong.imlib.RongIMClient;

/**
 * Created by ywwynm on 2016/5/13.
 * 自定义的Application类
 */
public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        // 初始化融云
        RongIMClient.init(this);
    }

    public static App getApp() {
        return app;
    }
}
