package com.room517.chitchat.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ywwynm on 2016/5/15.
 * 用户的模型类
 */
public class User {

    public static final int SEX_PRIVATE = 0;
    public static final int SEX_MAN     = 1;
    public static final int SEX_GIRL    = 2;

    @IntDef({SEX_PRIVATE, SEX_MAN, SEX_GIRL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Sex{}

    // 标识符
    private String id;

    // 昵称
    private String name;

    // 性别
    @Sex
    private int sex;

    // 标签
    private String tag;

    // 所在经度
    private double longitude;

    // 所在纬度
    private double latitude;

    // 创建时间
    private long createTime;

    public User(String id, String name, int sex, String tag,
                double longitude, double latitude, long createTime) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.tag = tag;
        this.longitude = longitude;
        this.latitude = latitude;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Sex int getSex() {
        return sex;
    }

    public void setSex(@Sex int sex) {
        this.sex = sex;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
