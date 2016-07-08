package com.room517.chitchat.utils;

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
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}
