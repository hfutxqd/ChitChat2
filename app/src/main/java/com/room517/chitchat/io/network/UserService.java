package com.room517.chitchat.io.network;

import com.room517.chitchat.model.User;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

import static com.room517.chitchat.Def.DB.TableUser.ID;
import static com.room517.chitchat.Def.DB.TableUser.LATITUDE;
import static com.room517.chitchat.Def.DB.TableUser.LONGITUDE;

/**
 * Created by ywwynm on 2016/5/18.
 * 用于retrofit，所有与User相关的网络操作都与该接口相关
 */
public interface UserService {

    @POST("index.php?c=index&a=login")
    Observable<ResponseBody> upload(@Body User newUser);

    @FormUrlEncoded
    @POST("index.php?c=index&a=getUserById")
    Observable<User> getUserById(@Field(ID) String userId);

    @FormUrlEncoded
    @POST("index.php?c=index&a=getNearbyUsers")
    Observable<ResponseBody> getNearbyUsers(
            @Field(ID)        String userId,
            @Field(LONGITUDE) double longitude,
            @Field(LATITUDE)  double latitude);

    @FormUrlEncoded
    @POST("index.php?c=index&a=logout")
    Observable<ResponseBody> logout(@Field(ID) String userId);

    @POST("index.php?c=index&a=getUsersByIds")
    Observable<User[]> getUsersByIds(@Body String[] userIds);

    @POST("index.php?c=index&a=update")
    Observable<ResponseBody> update(@Body User user);

}
