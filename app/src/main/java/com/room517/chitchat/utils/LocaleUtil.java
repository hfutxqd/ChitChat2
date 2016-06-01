package com.room517.chitchat.utils;

import com.room517.chitchat.App;

import java.util.Locale;

/**
 * Created by ywwynm on 2016/5/31.
 * 用于本地化、国际化
 */
public class LocaleUtil {

    public static boolean isChinese() {
        return isSimplifiedChinese() || isTraditionalChinese();
    }

    public static boolean isSimplifiedChinese() {
        Locale locale = App.getApp().getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage());
    }

    public static boolean isTraditionalChinese() {
        Locale locale = App.getApp().getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.TRADITIONAL_CHINESE.getLanguage());
    }

    public static boolean isEnglish() {
        Locale locale = App.getApp().getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.ENGLISH.getLanguage());
    }

}
