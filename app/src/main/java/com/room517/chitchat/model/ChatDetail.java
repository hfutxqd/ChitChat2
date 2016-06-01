package com.room517.chitchat.model;

import android.database.Cursor;
import android.support.annotation.IntDef;

import com.room517.chitchat.db.ChatDao;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

import static com.room517.chitchat.Def.DB.TableChatDetail.CONTENT;
import static com.room517.chitchat.Def.DB.TableChatDetail.FROM_ID;
import static com.room517.chitchat.Def.DB.TableChatDetail.ID;
import static com.room517.chitchat.Def.DB.TableChatDetail.STATE;
import static com.room517.chitchat.Def.DB.TableChatDetail.TIME;
import static com.room517.chitchat.Def.DB.TableChatDetail.TO_ID;

/**
 * Created by ywwynm on 2016/5/26.
 * 表chat_detail的模型类
 */
public class ChatDetail {

    public static final int STATE_NORMAL      = 0;
    public static final int STATE_SENDING     = 1;
    public static final int STATE_SEND_FAILED = 2;

    @IntDef({STATE_NORMAL, STATE_SENDING, STATE_SEND_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private long id;

    private String fromId;

    private String toId;

    @State
    private int state;

    private String content;

    private long time;

    public ChatDetail(long id, String fromId, String toId, @State int state, String content, long time) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.state = state;
        this.content = content;
        this.time = time;
    }

    public ChatDetail(Cursor cursor) {
        id      = cursor.getLong  (cursor.getColumnIndex(ID));
        fromId  = cursor.getString(cursor.getColumnIndex(FROM_ID));
        toId    = cursor.getString(cursor.getColumnIndex(TO_ID));
        state   = cursor.getInt   (cursor.getColumnIndex(STATE));
        content = cursor.getString(cursor.getColumnIndex(CONTENT));
        time    = cursor.getLong  (cursor.getColumnIndex(TIME));
    }

    public ChatDetail(Message message) {
        id      = ChatDao.getInstance().getNewChatDetailId();
        fromId  = message.getSenderUserId();
        toId    = message.getTargetId();
        state   = STATE_NORMAL;
        content = ((TextMessage) message.getContent()).getContent();
        time    = message.getSentTime();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public @State int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
