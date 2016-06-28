package com.room517.chitchat.utils;

/**
 * Created by imxqd on 2016/6/28.
 *
 */
public class Debug {
    private static final boolean DEBUG = true;

    public static void d(String s){
        if(DEBUG){
            System.out.println(s);
        }
    }
}
