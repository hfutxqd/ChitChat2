package com.room517.chitchat.io.network;

import com.room517.chitchat.model.Comment;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.Like;
import com.room517.chitchat.model.User;

import java.io.File;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈数据获取
 */
public interface ExploreService {
    @GET("index.php?c=index&a=ListExplore")
    Observable<ResponseBody> ListExplore(@Field("page") String page);

    @GET("index.php?c=index&a=ListComment")
    Observable<ResponseBody> ListComment(@Field("id") String exploreId);

    @Multipart
    @POST("index.php?c=index&a=upload")
    Observable<ResponseBody> upload(@Part("image") RequestBody image);

    @POST("index.php?c=index&a=publish")
    Observable<ResponseBody> publish(@Body Explore explore);

    @POST("index.php?c=index&a=comment")
    Observable<ResponseBody> comment(@Body Comment comment);

    @POST("index.php?c=index&a=like")
    Observable<ResponseBody> like(@Body Like like);

    @POST("index.php?c=index&a=unlike")
    Observable<ResponseBody> unlike(@Body Like like);
}
