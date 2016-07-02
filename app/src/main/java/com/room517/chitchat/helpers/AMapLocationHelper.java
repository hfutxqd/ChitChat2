package com.room517.chitchat.helpers;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * Created by imxqd on 2016/7/1.
 * 高德地图定位的封装类
 */
public class AMapLocationHelper implements AMapLocationListener {
    private static final String TAG = "AMapLocationHelper";

    private static AMapLocationHelper helper = null;

    private static final Location location = new Location();
    private AMapLocationClient locationClient;
    private AMapLocationClientOption option;
    private CallBack mCallBack = null;

    private AMapLocationHelper(Application app){
        locationClient = new AMapLocationClient(app);
        option = new AMapLocationClientOption();
        locationClient.setLocationListener(this);
        initOption(true, false, true, true, true, 10000);
        locationClient.setLocationOption(option);
    }

    public synchronized static AMapLocationHelper init(Application app){
        if(helper == null){
            helper = new AMapLocationHelper(app);
        }
        return helper;
    }


    /**
     * 初始化定位配置的方法
     * @param address 是否需要显示地址信息
     * @param gpsFirst 是否优先返回GPS定位结果
     * @param cacheable 是否开启缓存
     * @param once 是否单次定位
     * @param onceLast 是否返回最近3s内精度最高的一次定位结果
     * @param interval 发送定位请求的时间间隔
     */
    public void initOption(boolean address, boolean gpsFirst, boolean cacheable, boolean once
            , boolean onceLast, int interval) {
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setNeedAddress(address);
        option.setGpsFirst(gpsFirst);
        option.setLocationCacheEnable(cacheable);
        option.setOnceLocation(once);
        option.setOnceLocationLatest(onceLast);
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
        Log.d(TAG, "onLocationChanged:"+aMapLocation.toString());
    }

    /**
     * 使用当前的配置开始定位
     */
    public void startLocation(){
        locationClient.setLocationOption(option);
        locationClient.startLocation();
        Log.d(TAG, "startLocation");
    }

    /**
     * 停止定位
     */
    public void stopLocation(){
        locationClient.stopLocation();
        Log.d(TAG, "stopLocation");
    }

    /**
     * 销毁定位客户端
     */
    public void destroy(){
        locationClient.onDestroy();
        helper = null;
        Log.d(TAG, "destroy");
    }

    /**
     * 获取位置信息的非阻塞方法,提供回调接口
     * @param callBack  回调接口{@link CallBack}在onFinish方法中返回位置信息
     * {@link com.amap.api.location.AMapLocation}
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
     * 获取最后一次定位的位置
     * @return 最后一次定位的位置信息
     */
    @Nullable
    public AMapLocation getLastKnownLocation(){
        return locationClient.getLastKnownLocation();
    }

    /**
     * 非阻塞定位的回调接口
     */
    public interface CallBack{
        void onFinish(AMapLocation location);
    }
}
