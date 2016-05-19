package com.room517.chitchat.helper;

import com.room517.chitchat.Def;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ywwynm on 2016/5/18.
 * 一个对Retrofit的封装类
 */
public class RetrofitHelper {

    /**
     * 获得基于{@link Def.Network#BASE_URL}的{@link Retrofit}实例，使用Gson解析，并使用RxJava作为结果
     * @return 一个符合要求的{@link Retrofit}实例
     */
    public static Retrofit getBaseUrlRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new HttpLoggingInterceptor()
                                .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .baseUrl(Def.Network.BASE_URL)
                .build();
    }

}
