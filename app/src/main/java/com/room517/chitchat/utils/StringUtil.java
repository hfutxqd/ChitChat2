package com.room517.chitchat.utils;

import java.util.Random;

/**
 * Created by ywwynm on 2016/7/5.
 * utils for String
 */
public class StringUtil {

    public static String generateRandomString(long seed, int length) {
        Random random = new Random(seed);
        // String that I loved.
        String alphaNumeric =
                "0123456789abcdefghijklmnopqrstuvwxyzABCCDEFGHIJJKLMNOPQQRSTUVWXYZ";
        int bound = alphaNumeric.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length / 2; i++) {
            sb.append(alphaNumeric.charAt(random.nextInt(bound)));
        }
        return sb.toString();
    }

    public static int countOf(String src, char... chs) {
        int count = 0;
        final int len = src.length();
        for (int i = 0; i < len; i++) {
            char ch = src.charAt(i);
            for (char c : chs) {
                if (ch == c) {
                    count++;
                }
            }
        }
        return count;
    }

}
