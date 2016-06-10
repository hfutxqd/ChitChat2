package com.room517.chitchat.io.network;

import com.room517.chitchat.model.Comment;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.Like;
import com.room517.chitchat.model.User;

import java.io.File;
import java.util.ArrayList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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
    @FormUrlEncoded
    @POST("index.php?a=ListExplore")
    Observable<ArrayList<Explore>> ListExplore(@Field("id") String id, @Field("device_id") String device_id);

    @FormUrlEncoded
    @POST("index.php?a=ListComment")
    Observable<ArrayList<Comment>> ListComment(@Field("id") String exploreId);

    @POST("index.php?a=publish")
    Observable<ResponseBody> publish(@Body Explore explore);

    @POST("index.php?a=comment")
    Observable<ResponseBody> comment(@Body Comment comment);

    @POST("index.php?a=like")
    Observable<ResponseBody> like(@Body Like like);

    @POST("index.php?a=unlike")
    Observable<ResponseBody> unlike(@Body Like like);
}
