package com.room517.chitchat.utils;

import android.util.Log;

import com.room517.chitchat.BuildConfig;

import java.util.HashMap;

/**
 * Created by ywwynm on 2016/5/15.
 * 用于输出调试日志信息的类
 */
public class ClassLogger {

    private static HashMap<Class<?>, ClassLogger> loggerMap = new HashMap<>();

    public static ClassLogger getInstance(Class<?> clazz) {
        ClassLogger classLogger = loggerMap.get(clazz);
        if (classLogger == null) {
            classLogger = new ClassLogger(clazz.getSimpleName());
            loggerMap.put(clazz, classLogger);
        }
        return classLogger;
    }

    public void i(Object object) {
        if (BuildConfig.DEBUG) {
            Log.i(mTag, object.toString());
        }
    }

    public void e(Object object) {
        if (BuildConfig.DEBUG) {
            Log.i(mTag, object.toString());
        }
    }

    private String mTag;

    private ClassLogger(String tag) {
        mTag = tag;
    }

}
