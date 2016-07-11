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

    public static boolean deleteFile(String pathName) {
        File file = new File(pathName);
        return deleteFile(file);
    }

    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            return deleteDirectory(file);
        } else {
            return file.delete();
        }
    }

    public static boolean deleteDirectory(String pathName) {
        File dir = new File(pathName);
        return deleteDirectory(dir);
    }

    public static boolean deleteDirectory(File dir) {
        if (!dir.isDirectory()) {
            return false;
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            boolean deleted = deleteFile(file);
            if (!deleted) return false;
        }
        return dir.delete();
    }

}
