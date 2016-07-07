package com.room517.chitchat.utils;

import org.joda.time.DateTime;

import java.io.File;

/**
 * Created by ywwynm on 2016/7/7.
 * Utils for file
 */
public class FileUtil {

    public static String newSimpleFileName() {
        return DateTime.now().toString("yyyyMMddHHmmss");
    }

    public static File createFile(String parentPath, String filenameWithPostfix) {
        File parent = new File(parentPath);
        if (!parent.exists()) {
            boolean parentCreated = parent.mkdirs();
            if (!parentCreated) {
                return null;
            }
        }
        return new File(parent, filenameWithPostfix);
    }

}
