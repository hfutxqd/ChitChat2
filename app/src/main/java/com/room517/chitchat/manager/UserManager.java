package com.room517.chitchat.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.Logger;

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

    private Context mContext;

    private Logger mLogger;

    private UserManager() {
        mContext = App.getApp();
        mLogger = Logger.getInstance(getClass());
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
        editor.putString(Def.KEY.PrefUserMe.ID,        user.getId());
        editor.putString(Def.KEY.PrefUserMe.NAME,      user.getName());
        editor.putInt(Def.KEY.PrefUserMe.SEX,          user.getSex());
        editor.putString(Def.KEY.PrefUserMe.TAG,       user.getTag());
        editor.putString(Def.KEY.PrefUserMe.LOCATION,  user.getLocation());
        editor.putLong(Def.KEY.PrefUserMe.CREATE_TIME, user.getCreateTime());
        editor.apply();
    }

    /**
     * 从本地获得用户信息
     * @return 本地保存的用户
     */
    public User getUserFromLocal() {
        SharedPreferences sp = getPrefUserMe();
        String id       = sp.getString(Def.KEY.PrefUserMe.ID, "");
        String name     = sp.getString(Def.KEY.PrefUserMe.NAME, "");
        int sex         = sp.getInt(Def.KEY.PrefUserMe.SEX, 0);
        String tag      = sp.getString(Def.KEY.PrefUserMe.TAG, "");
        String location = sp.getString(Def.KEY.PrefUserMe.LOCATION, "");
        long createTime = sp.getLong(Def.KEY.PrefUserMe.CREATE_TIME, -1);
        return new User(id, name, sex, tag, location, createTime);
    }

    /**
     * 将用户信息上传到服务器
     * @param user 待上传信息的{@link User}对象
     */
    public void uploadUserInfoToServer(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User should not be null");
        }

        // TODO: 2016/5/15 将用户信息上传到服务器
    }

}
