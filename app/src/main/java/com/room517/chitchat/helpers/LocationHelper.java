package com.room517.chitchat.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.room517.chitchat.App;

import java.util.List;

/**
 * Created by ywwynm on 2016/5/16.
 * 帮助获取用户所在的地理位置信息
 */
@Deprecated
public class LocationHelper {

    /**
     * 从可用的地理位置提供者中获取最准确的位置信息
     *
     * 调用此方法时，必须在相应的Activity中获取以下权限中的一种：
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION}
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     *
     * @return 当前地理位置信息；如果无法获得，返回null
     */
    @Deprecated
    public static Location getLocation() {
        LocationManager lm = (LocationManager) App.getApp().getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location location = lm.getLastKnownLocation(provider);
            if (location == null) {
                location = getLocationAfterRequestingUpdate(lm, provider);
            }
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        return bestLocation;
    }

    /**
     * 获得存储当前地理位置信息的数组，第0个元素存储经度，第1个存储纬度
     * @return 存储地理位置信息的数组
     */
    @Deprecated
    public static double[] getLocationArray() {
        Location location = getLocation();
        if (location == null) {
            return null;
        } else {
            double longitude = location.getLongitude(); // 经度
            double latitude  = location.getLatitude();  // 纬度
            return new double[] { longitude, latitude };
        }
    }

    /**
     * 返回距离的描述性字符串
     * @param distance 距离
     * @return 距离的描述
     */
    @Deprecated
    @SuppressLint("DefaultLocale")
    public static String getDistanceDescription(int distance) {
        if (distance < 1000) {
            return distance + "m";
        } else {
            float km = distance / 1000f;
            return String.format("%.2f", km) + "km";
        }
    }

    private static Location getLocationAfterRequestingUpdate(LocationManager lm, String provider) {
        EmptyLocationListener listener = new EmptyLocationListener();
        lm.requestLocationUpdates(provider, 0, 0, listener);
        Location location = lm.getLastKnownLocation(provider);
        lm.removeUpdates(listener);
        return location;
    }

    private static class EmptyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
