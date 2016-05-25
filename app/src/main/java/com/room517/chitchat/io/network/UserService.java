package com.room517.chitchat.io.network;

import com.room517.chitchat.model.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

import static com.room517.chitchat.Def.DB.TableUser.*;

/**
 * Created by ywwynm on 2016/5/18.
 * 用于retrofit，所有与User相关的网络操作都与该接口相关
 */
public interface UserService {

    @POST("index.php?c=index&a=login")
    Observable<ResponseBody> upload(@Body User newUser);

    @FormUrlEncoded
    @POST("index.php?c=index&a=getNearbyUsers")
    Observable<List<User>> getNearbyUsers(
            @Field(ID) String userId,
            @Field(LONGITUDE) double longitude,
            @Field(LATITUDE) double latitude);

}
