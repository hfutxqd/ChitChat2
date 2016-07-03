package com.room517.chitchat.helpers;

import android.content.Intent;
import android.net.Uri;

import com.room517.chitchat.App;

import java.util.Locale;

/**
 * Created by imxqd on 2016/7/3.
 * 用于通过经纬度打开外部地图,并标记位置
 */
public class OpenMapHelper {
    public static void open(double longitude , double latitude, String addr){
        Uri mUri = Uri.parse(String.format(Locale.CHINESE, "geo:%f,%f(%s)", latitude, longitude, addr));
        Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getApp().startActivity(mIntent);
    }
}
