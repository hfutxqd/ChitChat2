package com.room517.chitchat.helpers;

import com.room517.chitchat.model.ChatDetail;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;

/**
 * Created by ywwynm on 2016/7/5.
 * 融云相关的帮助类
 */
public class RongHelper {

    public static void sendTextMessage(
            final ChatDetail chatDetail, RongIMClient.SendMessageCallback callback) {
        if (chatDetail.getType() != ChatDetail.TYPE_TEXT) {
            return;
        }

        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE, /* Conversation type */
                chatDetail.getToId(), /* targetId */
                TextMessage.obtain(chatDetail.toJson()), /* Message content */
                null, /* push content */
                null, /* push data */
                callback, /* SendMessageCallback */
                null /* ResultCallback<Message> */);
    }

    public static void sendCmdMessage(
            final ChatDetail chatDetail, RongIMClient.SendMessageCallback callback) {
        if (!chatDetail.isCmd()) {
            return;
        }

        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE, /* Conversation type */
                chatDetail.getToId(), /* targetId */
                TextMessage.obtain(chatDetail.toJson()), /* Message content */
                null, /* push content */
                null, /* push data */
                callback, /* SendMessageCallback */
                null /* ResultCallback<Message> */);
    }

}
