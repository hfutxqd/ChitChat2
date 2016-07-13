package com.room517.chitchat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.room517.chitchat.helpers.DBHelper;
import com.room517.chitchat.model.User;

import static com.room517.chitchat.Def.DB.TableUser.AVATAR;
import static com.room517.chitchat.Def.DB.TableUser.CREATE_TIME;
import static com.room517.chitchat.Def.DB.TableUser.ID;
import static com.room517.chitchat.Def.DB.TableUser.LATITUDE;
import static com.room517.chitchat.Def.DB.TableUser.LONGITUDE;
import static com.room517.chitchat.Def.DB.TableUser.NAME;
import static com.room517.chitchat.Def.DB.TableUser.SEX;
import static com.room517.chitchat.Def.DB.TableUser.TAG;
import static com.room517.chitchat.Def.DB.TableUser.COVER_URL;
import static com.room517.chitchat.Def.DB.TableUser.TableName;

/**
 * Created by ywwynm on 2016/5/25.
 * 表user的dao层操作类
 */
public class UserDao {

    private static UserDao sInstance;

    public static UserDao getInstance() {
        if (sInstance == null) {
            synchronized (UserDao.class) {
                if (sInstance == null) {
                    sInstance = new UserDao();
                }
            }
        }
        return sInstance;
    }

    private SQLiteDatabase db;

    private UserDao() {
        db = new DBHelper().getWritableDatabase();
    }

    /**
     * 通过id获取数据库中的用户对象
     * @param id 用户id
     */
    public User getUserById(@NonNull String id) {
        String selection = ID + "= '" + id + "'";
        Cursor cursor = db.query(TableName, null, selection, null, null, null, null);
        User user = null;
        if (cursor.moveToNext()) {
            user = new User(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * 向数据库中插入用户，该方法可能会在不应该调用的时候调用（我会在每个对话产生的时候都调用该方法，以避免判断数据库
     * 中是否已经存在该用户了），但没有关系，抛出的异常不会导致应用crash
     * @param user 待插入的用户对象
     * @return 插入成功返回 {@code true}，否则返回 {@code false}
     */
    public boolean insert(@NonNull User user) {
        ContentValues values = getUserDetailValues(user);
        values.put(ID,          user.getId());
        values.put(CREATE_TIME, user.getCreateTime());

        return db.insert(TableName, null, values) != -1;
    }

    /**
     * 在数据库中更新用户信息
     * @param user 更新后的用户对象，id、createTime应当一致，其它属性可以发生变化
     * @return 更新成功返回 {@code true}，否则返回{@code false}
     */
    public boolean update(@NonNull User user) {
        ContentValues values = getUserDetailValues(user);

        String where = ID + "='" + user.getId() + "'";
        return db.update(TableName, values, where, null) == 1;
    }

    private ContentValues getUserDetailValues(@NonNull User user) {
        ContentValues values = new ContentValues();
        values.put(NAME,        user.getName());
        values.put(SEX,         user.getSex());
        values.put(AVATAR,      user.getAvatar());
        values.put(TAG,         user.getTag());
        values.put(LONGITUDE,   user.getLongitude());
        values.put(LATITUDE,    user.getLatitude());
        values.put(COVER_URL,   user.getCoverUrl());
        return values;
    }

}
