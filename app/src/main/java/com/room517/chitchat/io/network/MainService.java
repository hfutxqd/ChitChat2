package com.room517.chitchat.io.network;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by ywwynm on 2016/5/18.
 * 用于retrofit，获取融云Token、网络时间
 */
public interface MainService {

    @FormUrlEncoded
    @POST("index.php?c=index&a=getToken")
    Observable<ResponseBody> getRongToken(
            @Field("id")     String userId,
            @Field("name")   String name,
            @Field("avatar") String avatar);

    @GET("index.php?c=index&a=getTime")
    Observable<ResponseBody> getCurrentTime();

}
