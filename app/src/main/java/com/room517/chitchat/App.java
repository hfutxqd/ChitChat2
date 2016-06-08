package com.room517.chitchat;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.helpers.CrashHelper;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.fragments.ChatDetailsFragment;
import com.room517.chitchat.ui.fragments.ChatListFragment;

import java.lang.ref.WeakReference;

import io.rong.imlib.RongIMClient;

/**
 * Created by ywwynm on 2016/5/13.
 * 自定义的Application类
 */
public class App extends Application {

    private static App app;

    // “我”的User类实例
    private static User me;

    private static WeakReference<ChatListFragment>    wrChatList;
    private static WeakReference<ChatDetailsFragment> wrChatDetails;

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
        // added by IMXQD
        Fresco.initialize(this);
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

    public static void setWrChatList(ChatListFragment chatListFragment) {
        wrChatList = new WeakReference<>(chatListFragment);
    }

    public static ChatListFragment getChatListReference() {
        if (wrChatList != null) {
            return wrChatList.get();
        }
        return null;
    }

    public static void setWrChatDetails(ChatDetailsFragment chatDetailsFragment) {
        wrChatDetails = new WeakReference<>(chatDetailsFragment);
    }

    public static boolean shouldNotifyMessage(String userId) {
        if (isTalkingWith(userId)) {
            return false;
        }

        if (wrChatList != null) {
            if (wrChatList.get() != null) { // 聊天列表fragment存在
                if (wrChatDetails == null || wrChatDetails.get() == null) {
                    // 聊天列表fragment在顶层
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isTalkingWith(String userId) {
        if (wrChatDetails != null) {
            ChatDetailsFragment cdf = wrChatDetails.get();
            if (cdf != null) {
                return cdf.getChat().getUserId().equals(userId);
            }
        }
        return false;
    }

    private static String getCurProcessName() {
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
