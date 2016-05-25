package com.room517.chitchat.model;

import android.database.Cursor;

import static com.room517.chitchat.Def.DB.TableChat.*;

/**
 * Created by ywwynm on 2016/5/25.
 * 对话的模型类
 */
public class Chat {

    private int    id;
    private String userId;

    public Chat(int id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public Chat(Cursor cursor) {
        id     = cursor.getInt(cursor.getColumnIndex(ID));
        userId = cursor.getString(cursor.getColumnIndex(USER_ID));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
