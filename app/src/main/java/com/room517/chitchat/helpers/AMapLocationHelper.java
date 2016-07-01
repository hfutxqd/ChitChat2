package com.room517.chitchat.helpers;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.room517.chitchat.App;

/**
 * Created by imxqd on 2016/7/1.
 * 高德地图定位
 */
public class AMapLocationHelper implements AMapLocationListener {
    private static final Location location = new Location();
    private AMapLocationClient locationClient;
    private AMapLocationClientOption option;
    private CallBack mCallBack = null;

    public AMapLocationHelper(){
        locationClient = new AMapLocationClient(App.getApp());
        option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(true);
        locationClient.setLocationListener(this);
    }

    private void initOption(boolean address, boolean gpsFirst, boolean cacheable, boolean once, int interval) {
        // 设置是否需要显示地址信息
        option.setNeedAddress(address);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        option.setGpsFirst(gpsFirst);
        // 设置是否开启缓存
        option.setLocationCacheEnable(cacheable);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        option.setOnceLocationLatest(once);
        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        option.setInterval(interval);

    }
    static class Location{
        AMapLocation location = null;
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation.getAddress().length() > 0){
            if(mCallBack != null){
                mCallBack.onFinish(aMapLocation);
                mCallBack = null;
                return;
            }
            synchronized (location){
                location.location = aMapLocation;
                location.notifyAll();
            }
        }
    }

    public void startLocation(){
        initOption(true, false, true, true, 10000);
        locationClient.setLocationOption(option);
        locationClient.startLocation();
    }


    public void getLocation(CallBack callBack){
        startLocation();
        mCallBack = callBack;
    }

    public AMapLocation getLocationSync(){
        startLocation();
        if(location.location == null){
            synchronized (location){
                try {
                    location.wait();
                    return location.location;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return location.location;
    }

    public interface CallBack{
        void onFinish(AMapLocation location);
    }
}
