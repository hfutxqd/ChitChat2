package com.room517.chitchat;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.helpers.CrashHelper;
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

        CrashHelper.getInstance().init();

        app = this;
        me  = UserManager.getInstance().getUserFromLocal();

        // OnCreate 会被多个进程重入，这段保护代码，确保只有需要使用 RongIMClient 的进程和 Push 进程执行了 init
        String curProcessName = getCurProcessName();
        if (getApplicationInfo().packageName.equals(curProcessName) ||
                "io.rong.push".equals(curProcessName)) {
            RongIMClient.init(this);
        }
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

    public static String getCurProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) app
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
