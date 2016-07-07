package com.room517.chitchat.model;

import android.database.Cursor;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static com.room517.chitchat.Def.DB.TableChat.*;

/**
 * Created by ywwynm on 2016/5/25.
 * 对话的模型类
 */
public class Chat {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_STICKY = 1;

    @IntDef({TYPE_NORMAL, TYPE_STICKY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    private String userId;

    private int type;

    private List<ChatDetail> mChatDetailsToDisplay;

    public Chat(String userId, @Type int type) {
        this.userId = userId;
        this.type   = type;
        mChatDetailsToDisplay = new ArrayList<>();
    }

    public Chat(Cursor cursor) {
        userId = cursor.getString(cursor.getColumnIndex(USER_ID));
        type   = cursor.getInt   (cursor.getColumnIndex(TYPE));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public @Chat.Type int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public List<ChatDetail> getChatDetailsToDisplay() {
        return mChatDetailsToDisplay;
    }

    public void setChatDetailsToDisplay(List<ChatDetail> chatDetailsToDisplay) {
        mChatDetailsToDisplay = chatDetailsToDisplay;
    }

    public int indexOfChatDetail(String id) {
        if (mChatDetailsToDisplay == null || TextUtils.isEmpty(id)) {
            return -1;
        }

        final int size = mChatDetailsToDisplay.size();
        for (int i = 0; i < size; i++) {
            ChatDetail cd = mChatDetailsToDisplay.get(i);
            if (cd.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !o.getClass().equals(Chat.class)) {
            return false;
        }

        Chat chat = (Chat) o;
        return userId.equals(chat.getUserId()) && type == chat.getType();
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + type;
        return result;
    }
}
