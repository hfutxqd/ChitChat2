package com.room517.chitchat.utils;

import android.os.Build;
import android.provider.Settings;

import com.room517.chitchat.App;

/**
 * Created by ywwynm on 2016/5/15.
 * 关于设备的信息
 */
public class DeviceUtil {

    public static boolean hasJellyBeanApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKatApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipopApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallowApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 获得设备的ANDROID_ID
     * 可能用处：作为新用户的标识符
     * @return 设备的ANDROID_ID
     */
    public static String getAndroidId() {
        return Settings.Secure.getString(
                App.getApp().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceInfo() {
        return "OS Version:   " + getAndroidVersion() + "\n" +
               "Manufacturer: " + getManufacturer()   + "\n" +
               "Phone Model:  " + getPhoneModel()     + "\n";
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT;
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getPhoneModel() {
        return Build.MODEL;
    }

}
