package com.room517.chitchat.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.room517.chitchat.helpers.DBHelper;

import static com.room517.chitchat.Def.DB.TableChat.*;

/**
 * Created by ywwynm on 2016/5/25.
 * 表chat的dao操作类
 */
public class ChatDao {

    private static ChatDao sInstance;

    public static ChatDao getInstance() {
        if (sInstance == null) {
            synchronized (ChatDao.class) {
                if (sInstance == null) {
                    sInstance = new ChatDao();
                }
            }
        }
        return sInstance;
    }

    private SQLiteDatabase db;

    private ChatDao() {
        db = new DBHelper().getWritableDatabase();
    }

    public boolean noChat() {
        Cursor cursor = db.query(TableName, null, null, null, null, null, null);
        int count = 0;
        while (cursor.moveToNext()) {
            count++;
            if (count == 1) {
                break;
            }
        }
        cursor.close();
        return count == 0;
    }

}
