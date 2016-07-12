package com.room517.chitchat.utils;

import com.amap.api.location.AMapLocation;
import com.room517.chitchat.App;
import com.room517.chitchat.R;

import java.util.Locale;

/**
 * Created by imxqd on 2016/7/7.
 * 位置操作的工具类
 */
public class LocationUtil {
    private static final double EARTH_RADIUS = 6378.137;//地球半径

    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    /**
     * 根据经纬度计算距离
     * @param lat1 维度1
     * @param lng1 经度1
     * @param lat2 维度2
     * @param lng2 经度2
     * @return 距离
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        float[] res = new float[1];
        AMapLocation.distanceBetween(lat1, lng1, lat2, lng2, res);
        System.out.println(res[0]);
        return res[0];
    }

    public static String distanceToString(double distance) {
        if(distance < 1000) {
            String str = String.format(Locale.CHINESE, "%.0f", distance);
            return str + App.getApp().getString(R.string.location_distance_unit_m);
        } else {
            String str = String.format(Locale.CHINESE, "%.2f", distance / 1000);
            return str + App.getApp().getString(R.string.location_distance_unit_km);
        }
    }
}
