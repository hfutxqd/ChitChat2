package com.room517.chitchat.helpers;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;

/**
 * Created by imxqd on 2016/7/1.
 * 高德地图定位的封装类
 */
public class AMapLocationHelper implements AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener
        , PoiSearch.OnPoiSearchListener {

    private static final String TAG = "AMapLocationHelper";

    private static final Location location = new Location();

    private static AMapLocationHelper helper = null;

    private AMapLocationClient locationClient;
    private AMapLocationClientOption option;

    private AMapLocationCallBack mAMapLocationCallBack = null;
    private AddrPointInfoCallBack addrPointInfoCallBack = null;
    private AddrPonitsCallBack addrPonitsCallBack = null;

    private AMapLocationHelper(Application app) {
        locationClient = new AMapLocationClient(app);
        option = new AMapLocationClientOption();
        locationClient.setLocationListener(this);
        initOption(true, false, true, true, true, 10000);
        locationClient.setLocationOption(option);
    }

    /**
     * 初始化定位配置的方法
     *
     * @param address  是否需要显示地址信息
     * @param gpsFirst 是否优先返回GPS定位结果
     * @param useCache 是否开启缓存
     * @param once     是否单次定位
     * @param onceLast 是否返回最近3s内精度最高的一次定位结果
     * @param interval 发送定位请求的时间间隔
     */
    public void initOption(boolean address, boolean gpsFirst, boolean useCache, boolean once
            , boolean onceLast, int interval) {
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setNeedAddress(address);
        option.setGpsFirst(gpsFirst);
        option.setLocationCacheEnable(useCache);
        option.setOnceLocation(once);
        option.setOnceLocationLatest(onceLast);
        option.setInterval(interval);
    }

    public synchronized static AMapLocationHelper init(Application app) {
        if (helper == null) {
            helper = new AMapLocationHelper(app);
        }
        return helper;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Logger.i(aMapLocation.getErrorCode() + "");
        Logger.i(aMapLocation.getErrorInfo());
        if (aMapLocation.getAddress().length() > 0) {
            if (mAMapLocationCallBack != null) {
                mAMapLocationCallBack.onAMapLocationFinish(aMapLocation);
                mAMapLocationCallBack = null;
                return;
            }
            synchronized (location) {
                location.location = aMapLocation;
                location.notifyAll();
            }
        }
        Log.d(TAG, "onLocationChanged:" + aMapLocation.toString());
    }

    /**
     * 停止定位
     */
    public void stopLocation() {
        locationClient.stopLocation();
        Log.d(TAG, "stopLocation");
    }

    /**
     * 销毁定位客户端
     */
    public void destroy() {
        locationClient.onDestroy();
        helper = null;
        Log.d(TAG, "destroy");
    }

    /**
     * 获取位置信息的非阻塞方法,提供回调接口
     *
     * @param AMapLocationCallBack 回调接口{@link AMapLocationCallBack}在onFinish方法中返回位置信息
     *                 {@link com.amap.api.location.AMapLocation}
     */
    public void getLocation(AMapLocationCallBack AMapLocationCallBack) {
        startLocation();
        mAMapLocationCallBack = AMapLocationCallBack;
    }

    /**
     * 使用当前的配置开始定位
     */
    public void startLocation() {
        locationClient.setLocationOption(option);
        locationClient.startLocation();
        Log.d(TAG, "startLocation");
    }

    /**
     * 获取位置信息的阻塞方法
     *
     * @return 位置信息, 类{@link AMapLocation} 的对象
     */
    @Nullable
    public AMapLocation getLocationSync() {
        startLocation();
        if (location.location == null) {
            synchronized (location) {
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
     *
     * @return 最后一次定位的位置信息
     */
    @Nullable
    public AMapLocation getLastKnownLocation() {
        return locationClient.getLastKnownLocation();
    }

    public void setAddrPointInfoCallBack(AddrPointInfoCallBack callBack) {
        addrPointInfoCallBack = callBack;
    }

    public void setAddrPonitsCallBack(AddrPonitsCallBack callBack) {
        addrPonitsCallBack = callBack;
    }


    public void getAddrPointInfo(double latitude, double longitude) {
        GeocodeSearch search =new GeocodeSearch(App.getApp());
        search.getFromLocationAsyn(new RegeocodeQuery(new LatLonPoint(latitude, longitude)
        ,300, ""));
    }

    public void getAddrPonits(double latitude, double longitude) {
        PoiSearch.Query query = new PoiSearch.Query("", "");
        query.setPageSize(20);
        PoiSearch search = new PoiSearch(App.getApp(),query);
        search.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude, longitude), 2000));
        search.setOnPoiSearchListener(this);
        search.searchPOIAsyn();
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.d(TAG, "onRegeocodeSearched " + regeocodeResult.toString());
        addrPointInfoCallBack.onGetAddrPonitInfoFinish(regeocodeResult);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        Log.d(TAG, "onGeocodeSearched " + geocodeResult.toString());
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        Log.d(TAG, "onPoiSearched " + poiResult.toString());
        addrPonitsCallBack.onGetAddrPonitsFinish(poiResult);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        Log.d(TAG, "onPoiItemSearched " + poiItem.toString());
    }


    /**
     * 非阻塞定位的回调接口
     */
    public interface AMapLocationCallBack {
        void onAMapLocationFinish(AMapLocation location);
    }

    /**
     * 获取最近信息点的回调接口
     */
    public interface AddrPonitsCallBack {
        void onGetAddrPonitsFinish(PoiResult poiResult);
    }

    /**
     * 获取位置点信息的回调接口
     */
    public interface AddrPointInfoCallBack {
        void onGetAddrPonitInfoFinish(RegeocodeResult regeocodeResult);
    }

    /**
     * {@link com.amap.api.location.AMapLocation}的封装类,用于获取定位信息的阻塞方法的实现
     */
    static class Location {
        AMapLocation location = null;
    }
}
