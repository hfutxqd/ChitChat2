package com.room517.chitchat.model;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.gson.Gson;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Random;

import static com.room517.chitchat.Def.DB.TableUser.AVATAR;
import static com.room517.chitchat.Def.DB.TableUser.CREATE_TIME;
import static com.room517.chitchat.Def.DB.TableUser.ID;
import static com.room517.chitchat.Def.DB.TableUser.LATITUDE;
import static com.room517.chitchat.Def.DB.TableUser.LONGITUDE;
import static com.room517.chitchat.Def.DB.TableUser.NAME;
import static com.room517.chitchat.Def.DB.TableUser.SEX;
import static com.room517.chitchat.Def.DB.TableUser.TAG;

/**
 * Created by ywwynm on 2016/5/15.
 * 用户的模型类
 */
public class User implements Parcelable {

    public static final int SEX_PRIVATE = 0;
    public static final int SEX_MAN     = 1;
    public static final int SEX_GIRL    = 2;

    @IntDef({SEX_PRIVATE, SEX_MAN, SEX_GIRL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Sex {}

    // 标识符
    private String id;

    // 昵称
    private String name;

    // 性别
    @Sex
    private int sex;

    private String avatar;

    // 标签
    private String tag;

    // 所在经度
    private double longitude;

    // 所在纬度
    private double latitude;

    // 创建时间
    private long createTime;

    public User(String id, String name, @Sex int sex, String avatar, String tag,
                double longitude, double latitude, long createTime) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.avatar = avatar;
        this.tag = tag;
        this.longitude = longitude;
        this.latitude = latitude;
        this.createTime = createTime;
    }

    public User(User user) {
        id         = user.id;
        name       = user.name;
        sex        = user.sex;
        avatar     = user.avatar;
        tag        = user.tag;
        longitude  = user.longitude;
        latitude   = user.latitude;
        createTime = user.createTime;
    }

    public User(Cursor cursor) {
        id         = cursor.getString(cursor.getColumnIndex(ID));
        name       = cursor.getString(cursor.getColumnIndex(NAME));
        sex        = cursor.getInt(   cursor.getColumnIndex(SEX));
        avatar     = cursor.getString(cursor.getColumnIndex(AVATAR));
        tag        = cursor.getString(cursor.getColumnIndex(TAG));
        longitude  = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
        latitude   = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
        createTime = cursor.getLong(  cursor.getColumnIndex(CREATE_TIME));
    }

    public User(Parcel in) {
        this.id         = in.readString();
        this.name       = in.readString();
        this.sex        = in.readInt();
        this.avatar     = in.readString();
        this.tag        = in.readString();
        this.longitude  = in.readDouble();
        this.latitude   = in.readDouble();
        this.createTime = in.readLong();
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    /**
     * 获得用户的颜色，该信息存储在了{@link #avatar}中
     * 提示：未来avatar可能会存储头像图片
     * @return 用户颜色
     */
    public int getColor() {
        if (isAvatarTextDrawable()) {
            return Integer.parseInt(avatar);
        }
        return 0;
    }

    /**
     * 获得用户头像的{@link Drawable}对象
     * 目前返回的均为{@link TextDrawable}类型
     */
    public Drawable getAvatarDrawable() {
        if (isAvatarTextDrawable()) {
            return getTextDrawableAsAvatar();
        }
        return null;
    }

    private boolean isAvatarTextDrawable() {
        return avatar != null;
    }

    private Drawable getTextDrawableAsAvatar() {
        return TextDrawable.builder()
                .buildRound(name.substring(0, 1), Integer.parseInt(avatar));
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public boolean equalsEditableData(User user) {
        return name.equals(user.name)
                && avatar.equals(user.avatar)
                && sex == user.sex
                && tag.equals(user.tag);
    }

    /**
     * 判断用户名是否合法
     * 规则：1至20个字符，只允许英文字母、数字、下划线、汉字的组合
     */
    public static String isNameValid(String name) {
        final int len = name.length();
        if (len < 1 || len > 20) {
            return App.getApp().getString(R.string.error_name_length);
        }

        if (name.matches("[a-zA-Z0-9_\u4e00-\u9fa5]*")) {
            return Def.Constant.VALID;
        } else {
            return App.getApp().getString(R.string.error_name_char);
        }
    }

    /**
     * 新用户注册时，我们从 Material Design 500的颜色中随机选一个作为其头像的背景色
     * @return 一个 Material Design 500 颜色
     */
    public static int getRandomColorAsAvatarBackground() {
        int[] colors = App.getApp().getResources().getIntArray(R.array.material_500);
        return colors[new Random().nextInt(colors.length)];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(sex);
        dest.writeString(avatar);
        dest.writeString(tag);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeLong(createTime);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
