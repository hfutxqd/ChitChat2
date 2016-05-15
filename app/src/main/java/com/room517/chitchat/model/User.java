package com.room517.chitchat.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ywwynm on 2016/5/15.
 * 用户的模型类
 */
public class User {

    public static final int SEX_MAN     = 0;
    public static final int SEX_GIRL    = 1;
    public static final int SEX_PRIVATE = 2;

    @IntDef({SEX_MAN, SEX_GIRL, SEX_PRIVATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Sex{}

    // 标识符
    private String mId;

    // 昵称
    private String mName;

    // 性别
    @Sex
    private int mSex;

    // 标签
    private String mTag;

    // 所在地点
    private String mLocation;

    // 创建时间
    private long mCreateTime;

    public User(String id, String name, int sex, String tag,
                String location, long createTime) {
        mId         = id;
        mName       = name;
        mSex        = sex;
        mTag        = tag;
        mLocation   = location;
        mCreateTime = createTime;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public @Sex int getSex() {
        return mSex;
    }

    public void setSex(@Sex int sex) {
        mSex = sex;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        mCreateTime = createTime;
    }
}
