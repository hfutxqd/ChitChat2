package com.room517.chitchat.helpers;

import android.support.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.room517.chitchat.App;

/**
 * Created by imxqd on 2016/7/1.
 * 高德地图定位的封装类
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
        initOption(true, false, true, true, 10000);
    }

    /**
     * 初始化定位配置的方法
     * @param address 是否需要显示地址信息
     * @param gpsFirst 是否优先返回GPS定位结果
     * @param cacheable 是否开启缓存
     * @param once 是否等待设备wifi刷新
     * @param interval 发送定位请求的时间间隔
     */
    public void initOption(boolean address, boolean gpsFirst, boolean cacheable, boolean once, int interval) {
        option.setNeedAddress(address);
        option.setGpsFirst(gpsFirst);
        option.setLocationCacheEnable(cacheable);
        option.setOnceLocationLatest(once);
        option.setInterval(interval);

    }

    /**
     * {@link com.amap.api.location.AMapLocation}的封装类,用于获取定位信息的阻塞方法的实现
     */
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

    /**
     * 使用当前的配置开始定位
     */
    public void startLocation(){
        locationClient.setLocationOption(option);
        locationClient.startLocation();
    }

    /**
     * 获取位置信息的非阻塞方法,提供回调接口
     * @param callBack  回调接口{@link CallBack}在onFinish方法中返回位置信息{@link com.amap.api.location.AMapLocation}
     */
    public void getLocation(CallBack callBack){
        startLocation();
        mCallBack = callBack;
    }

    /**
     * 获取位置信息的阻塞方法
     * @return 位置信息, 类{@link com.amap.api.location.AMapLocation} 的对象
     */
    @Nullable
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

    /**
     * 非阻塞定位的回调接口
     */
    public interface CallBack{
        void onFinish(AMapLocation location);
    }
}
