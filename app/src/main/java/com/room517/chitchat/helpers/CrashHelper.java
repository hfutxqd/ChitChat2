package com.room517.chitchat.helpers;

import android.annotation.SuppressLint;
import android.os.Process;

import com.room517.chitchat.BuildConfig;
import com.room517.chitchat.Def;
import com.room517.chitchat.utils.DeviceUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ywwynm on 2016/4/29.
 * helper for crash that caused by uncaught exceptions.
 */
public class CrashHelper implements Thread.UncaughtExceptionHandler {

    private static CrashHelper sCrashHelper;

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public static CrashHelper getInstance() {
        if (sCrashHelper == null) {
            synchronized (CrashHelper.class) {
                if (sCrashHelper == null) {
                    sCrashHelper = new CrashHelper();
                }
            }
        }
        return sCrashHelper;
    }

    private CrashHelper() { }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        saveCrashInfoToStorage(ex);

        ex.printStackTrace();

        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void saveCrashInfoToStorage(Throwable ex) {
        String path = Def.Meta.APP_DIR + "/log";
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String name = "crash_" + time + ".log";
        File file = createFile(path, name);
        if (file == null) {
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.println(time);
            writer.print("APP Version:  ");
            writer.println(BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE);
            writer.println(DeviceUtil.getDeviceInfo());
            writer.println();
            ex.printStackTrace(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createFile(String parentPath, String name) {
        File parent = new File(parentPath);
        if (!parent.exists()) {
            boolean parentCreated = parent.mkdirs();
            if (!parentCreated) {
                return null;
            }
        }
        return new File(parent, name);
    }
}
