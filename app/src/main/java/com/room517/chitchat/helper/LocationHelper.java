package com.room517.chitchat.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.room517.chitchat.App;

/**
 * Created by ywwynm on 2016/5/16.
 * 帮助获取用户所在的地理位置信息
 */
public class LocationHelper {

    /**
     * 获取位置信息
     * 当GPS可以使用时，使用GPS提供的地理位置信息；否则，使用网络定位获得的地理位置信息
     *
     * 调用此方法时，必须在相应的Activity中获取以下权限中的一种：
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION}
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     *
     * @return 当前地理位置信息；如果无法获得，返回null
     */
    public static Location getLocation() {
        LocationManager lm = (LocationManager) App.getApp().getSystemService(
                Context.LOCATION_SERVICE);
        final String GPS     = LocationManager.GPS_PROVIDER;
        final String NETWORK = LocationManager.NETWORK_PROVIDER;
        if (lm.isProviderEnabled(GPS)) {
            Location location = lm.getLastKnownLocation(GPS);
            if (location == null) {
                location = getLocationAfterRequestingUpdate(lm, GPS);
            }
            return location;
        } else if (lm.isProviderEnabled(NETWORK)) {
            return getLocationAfterRequestingUpdate(lm, NETWORK);
        }
        return null;
    }

    /**
     * 获得存储当前地理位置信息的数组，第0个元素存储经度，第1个存储纬度
     * @return 存储地理位置信息的数组
     */
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

    private static Location getLocationAfterRequestingUpdate(LocationManager lm, String provider) {
        EmptyLocationListener listener = new EmptyLocationListener();
        lm.requestLocationUpdates(provider, 2000, 10, listener);
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
