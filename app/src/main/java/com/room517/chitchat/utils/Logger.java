package com.room517.chitchat.utils;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by ywwynm on 2016/5/15.
 * 用于输出调试日志信息的类
 */
public class Logger {

    private static HashMap<Class<?>, Logger> loggerMap = new HashMap<>();

    public static Logger getInstance(Class<?> clazz) {
        Logger logger = loggerMap.get(clazz);
        if (logger == null) {
            logger = new Logger(clazz.getSimpleName());
            loggerMap.put(clazz, logger);
        }
        return logger;
    }

    public void i(Object object) {
        Log.i(mTag, object.toString());
    }

    private String mTag;

    private Logger(String tag) {
        mTag = tag;
    }

}
