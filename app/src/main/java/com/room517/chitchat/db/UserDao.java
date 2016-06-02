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

    public boolean insert(@NonNull User user) {
        ContentValues values = new ContentValues();
        values.put(ID,          user.getId());
        values.put(NAME,        user.getName());
        values.put(SEX,         user.getSex());
        values.put(AVATAR,      user.getAvatar());
        values.put(TAG,         user.getTag());
        values.put(LONGITUDE,   user.getLongitude());
        values.put(LATITUDE,    user.getLatitude());
        values.put(CREATE_TIME, user.getCreateTime());

        return db.insert(TableName, null, values) != -1;
    }

}
