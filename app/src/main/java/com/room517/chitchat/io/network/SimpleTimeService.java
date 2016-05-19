package com.room517.chitchat.io.network;

import com.room517.chitchat.model.SimpleTime;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by ywwynm on 2016/5/18.
 * 用于retrofit，获取网络时间
 */
public interface SimpleTimeService {

    @GET("index.php?c=index&a=getTime")
    Observable<SimpleTime> getCurrentTime();

}
