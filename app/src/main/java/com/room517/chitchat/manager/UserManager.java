package com.room517.chitchat.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.DeviceUtil;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by ywwynm on 2016/5/15.
 * 用户 {@link User} 的管理者类
 */
public class UserManager {

    private static UserManager sInstance;

    /**
     * 获得实例对象
     * @return UserManager实例
     */
    public static UserManager getInstance() {
        if (sInstance == null) {
            synchronized (UserManager.class) {
                if (sInstance == null) {
                    sInstance = new UserManager();
                }
            }
        }
        return sInstance;
    }

    public static String getNewUserId() {
        String androidId = DeviceUtil.getAndroidId();
        return androidId;
    }

    private Context mContext;

    private UserManager() {
        mContext = App.getApp();
    }

    public SharedPreferences getPrefUserMe() {
        return mContext.getSharedPreferences(
                Def.Meta.PREFERENCE_USER_ME, Context.MODE_PRIVATE);
    }

    /**
     * 将用户信息保存到{@link SharedPreferences}文件
     * @param user 待保存信息的{@link User}对象
     */
    public void saveUserInfoToLocal(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User should not be null");
        }

        SharedPreferences sp = getPrefUserMe();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Def.Key.PrefUserMe.ID,        user.getId())
              .putString(Def.Key.PrefUserMe.NAME,      user.getName())
              .putInt(Def.Key.PrefUserMe.SEX,          user.getSex())
              .putString(Def.Key.PrefUserMe.AVATAR,    user.getAvatar())
              .putString(Def.Key.PrefUserMe.TAG,       user.getTag())
              .putString(Def.Key.PrefUserMe.LONGITUDE, String.valueOf(user.getLongitude()))
              .putString(Def.Key.PrefUserMe.LATITUDE,  String.valueOf(user.getLatitude()))
              .putLong(Def.Key.PrefUserMe.CREATE_TIME, user.getCreateTime())
              .apply();
    }

    /**
     * 从本地获得用户信息
     * @return 本地保存的用户
     */
    public User getUserFromLocal() {
        SharedPreferences sp = getPrefUserMe();
        if (!sp.contains(Def.Key.PrefUserMe.ID)) {
            // TODO: 2016/5/26 remove these and return null
            User user = new User("96", "test", 0,
                    String.valueOf(User.getRandomColorAsAvatarBackground()), "", 0, 0,
                    System.currentTimeMillis());
            Logger.w("User info has not been stored to local");
            return user;
        }

        String id         = sp.getString(Def.Key.PrefUserMe.ID,     "");
        String name       = sp.getString(Def.Key.PrefUserMe.NAME,   "");
        int sex           = sp.getInt   (Def.Key.PrefUserMe.SEX,     0);
        String avatar     = sp.getString(Def.Key.PrefUserMe.AVATAR, "");
        String tag        = sp.getString(Def.Key.PrefUserMe.TAG,    "");
        double longitude  = Double.parseDouble(sp.getString(Def.Key.PrefUserMe.LONGITUDE, "0"));
        double latitude   = Double.parseDouble(sp.getString(Def.Key.PrefUserMe.LATITUDE,  "0"));
        long createTime   = sp.getLong(Def.Key.PrefUserMe.CREATE_TIME, -1);
        return new User(id, name, sex, avatar, tag, longitude, latitude, createTime);
    }

    /**
     * 将用户信息上传到服务器
     * @param user 待上传信息的{@link User}对象
     */
    public void uploadUserInfoToServer(User user, SimpleObserver<ResponseBody> observer) {
        if (user == null) {
            throw new IllegalArgumentException("User should not be null");
        }

        Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
        UserService service = retrofit.create(UserService.class);
        RxHelper.ioMain(service.upload(user), observer);
    }

}
