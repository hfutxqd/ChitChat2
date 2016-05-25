package com.room517.chitchat;

import android.app.Application;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.User;

import io.rong.imlib.RongIMClient;

/**
 * Created by ywwynm on 2016/5/13.
 * 自定义的Application类
 */
public class App extends Application {

    private static App app;

    // “我”的User类实例
    private static User me;

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init(Def.Meta.APP_NAME);

        app = this;
        me  = UserManager.getInstance().getUserFromLocal();

        // 初始化融云
        RongIMClient.init(this);
    }

    public static App getApp() {
        return app;
    }

    public static void setMe(User me) {
        App.me = me;
    }

    public static User getMe() {
        return me;
    }
}
