package com.room517.chitchat.utils;

import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Like;

import retrofit2.Retrofit;

/**
 * Created by imxqd on 2016/6/29.
 * 用于处理网络操作
 */
public class WebUtil {
    public static class Explore{
        public static void like(Like like, CallBack<Boolean> t){
            Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
            ExploreService service = retrofit.create(ExploreService.class);

        }
    }

    public interface CallBack<T>{
        void onError(Throwable e);
        void onSuccess(T t);
    }
}
