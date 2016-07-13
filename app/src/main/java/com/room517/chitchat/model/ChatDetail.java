package com.room517.chitchat.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.utils.StringUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
import io.rong.push.notification.PushNotificationMessage;

import static com.room517.chitchat.Def.DB.TableChatDetail.CONTENT;
import static com.room517.chitchat.Def.DB.TableChatDetail.FROM_ID;
import static com.room517.chitchat.Def.DB.TableChatDetail.ID;
import static com.room517.chitchat.Def.DB.TableChatDetail.STATE;
import static com.room517.chitchat.Def.DB.TableChatDetail.TIME;
import static com.room517.chitchat.Def.DB.TableChatDetail.TO_ID;
import static com.room517.chitchat.Def.DB.TableChatDetail.TYPE;

/**
 * Created by ywwynm on 2016/5/26.
 * 表chat_detail的模型类
 */
public class ChatDetail implements Parcelable {

    public static final int TYPE_TEXT     = 0;
    public static final int TYPE_IMAGE    = 1;
    public static final int TYPE_AUDIO    = 2;
    public static final int TYPE_LOCATION = 3;

    public static final int TYPE_CMD_WITHDRAW        = 100;
    public static final int TYPE_CMD_WITHDRAW_RESULT = 101;

    @IntDef({TYPE_TEXT, TYPE_IMAGE, TYPE_AUDIO, TYPE_LOCATION,
            TYPE_CMD_WITHDRAW, TYPE_CMD_WITHDRAW_RESULT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    public static final int STATE_NORMAL          = 0;
    public static final int STATE_SENDING         = 1;
    public static final int STATE_SEND_FAILED     = 2;
    public static final int STATE_WITHDRAWING     = 3;
    public static final int STATE_WITHDRAW_FAILED = 4;

    @IntDef({STATE_NORMAL, STATE_SENDING, STATE_SEND_FAILED,
            STATE_WITHDRAWING, STATE_WITHDRAW_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private String id;

    private String fromId;

    private String toId;

    @Type
    private int type;

    @State
    private int state;

    private String content;

    private long time;

    public ChatDetail(String id, String fromId, String toId,
                      @Type int type, @State int state, String content, long time) {
        this.id      = id;
        this.fromId  = fromId;
        this.toId    = toId;
        this.type    = type;
        this.state   = state;
        this.content = content;
        this.time    = time;
    }

    public ChatDetail(Cursor cursor) {
        id      = cursor.getString(cursor.getColumnIndex(ID));
        fromId  = cursor.getString(cursor.getColumnIndex(FROM_ID));
        toId    = cursor.getString(cursor.getColumnIndex(TO_ID));
        type    = cursor.getInt   (cursor.getColumnIndex(TYPE));
        state   = cursor.getInt   (cursor.getColumnIndex(STATE));
        content = cursor.getString(cursor.getColumnIndex(CONTENT));
        time    = cursor.getLong  (cursor.getColumnIndex(TIME));
    }

    public ChatDetail(Message message) {
        MessageContent mc = message.getContent();
        String json = "";

        if (mc instanceof TextMessage) {
            json = ((TextMessage) mc).getContent();
        } else if (mc instanceof ImageMessage) {
            json = ((ImageMessage) mc).getExtra();
        } else if (mc instanceof VoiceMessage) {
            json = ((VoiceMessage) mc).getExtra();
        }

        ChatDetail chatDetail = new Gson().fromJson(json, ChatDetail.class);
        id      = chatDetail.id;     // 可以认为这个id是独一无二的
        fromId  = chatDetail.fromId;
        toId    = chatDetail.toId;
        type    = chatDetail.type;
        state   = STATE_NORMAL;
        time    = chatDetail.time;

        if (mc instanceof TextMessage) { // location is also here
            content = chatDetail.content;
        } else if (mc instanceof ImageMessage) {
            Uri local = ((ImageMessage) mc).getLocalUri();
            content = (local != null ? local : ((ImageMessage) mc).getRemoteUri()).toString();
        } else if (mc instanceof VoiceMessage) {
            AudioInfo audioInfo = AudioInfo.fromJson(chatDetail.content);
            audioInfo.setUri(((VoiceMessage) mc).getUri().toString());
            content = audioInfo.toJson();
        }

        Logger.i("Received message. ChatDetail content: " + content);
    }

    public ChatDetail(PushNotificationMessage message) {
        String json = message.getPushContent();
        ChatDetail chatDetail = new Gson().fromJson(json, ChatDetail.class);
        id      = chatDetail.id;     // 可以认为这个id是独一无二的
        fromId  = chatDetail.fromId;
        toId    = chatDetail.toId;
        type    = chatDetail.type;
        state   = STATE_NORMAL;
        content = chatDetail.content;
        time    = chatDetail.time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public @Type int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
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

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean isCmd() {
        return type >= TYPE_CMD_WITHDRAW;
    }

    public boolean canCopy() {
        return type == TYPE_TEXT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatDetail that = (ChatDetail) o;

        if (type != that.type) return false;
        if (time != that.time) return false;
        if (!id.equals(that.id)) return false;
        if (!fromId.equals(that.fromId)) return false;
        if (!toId.equals(that.toId)) return false;
        return content.equals(that.content);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + fromId.hashCode();
        result = 31 * result + toId.hashCode();
        result = 31 * result + type;
        result = 31 * result + content.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    public static String newChatDetailId(String fromId) {
        return StringUtil.generateRandomString(
                fromId.hashCode() + System.currentTimeMillis(), 16);
    }

    public static ChatDetail newTempChatDetail(String id, String fromId, String toId) {
        return new ChatDetail(id, fromId, toId, ChatDetail.TYPE_TEXT, ChatDetail.STATE_NORMAL,
                "hello QQ", System.currentTimeMillis());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(fromId);
        dest.writeString(toId);
        dest.writeInt(type);
        dest.writeInt(state);
        dest.writeString(content);
        dest.writeLong(time);
    }

    protected ChatDetail(Parcel in) {
        this.id      = in.readString();
        this.fromId  = in.readString();
        this.toId    = in.readString();
        this.type    = in.readInt();
        this.state   = in.readInt();
        this.content = in.readString();
        this.time    = in.readLong();
    }

    public static final Parcelable.Creator<ChatDetail> CREATOR = new Parcelable.Creator<ChatDetail>() {
        @Override
        public ChatDetail createFromParcel(Parcel source) {
            return new ChatDetail(source);
        }

        @Override
        public ChatDetail[] newArray(int size) {
            return new ChatDetail[size];
        }
    };
}
