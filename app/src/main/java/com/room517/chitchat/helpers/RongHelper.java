package com.room517.chitchat.helpers;

import android.net.Uri;

import com.room517.chitchat.model.AudioInfo;
import com.room517.chitchat.model.ChatDetail;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

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

    public static void sendImageMessage(
            final ChatDetail chatDetail, RongIMClient.SendImageMessageCallback callback) {
        if (chatDetail.getType() != ChatDetail.TYPE_IMAGE) {
            return;
        }

        Uri uri = Uri.parse(chatDetail.getContent());
        ImageMessage im = ImageMessage.obtain(uri, uri);
        im.setExtra(chatDetail.toJson());

        RongIMClient.getInstance().sendImageMessage(
                Conversation.ConversationType.PRIVATE, /* Conversation type */
                chatDetail.getToId(), /* targetId */
                im, /* Message content */
                null, /* push content */
                null, /* push data */
                callback /* SendMessageCallback */);
    }

    public static void sendVoiceMessage(
            final ChatDetail chatDetail, RongIMClient.SendMessageCallback callback) {
        if (chatDetail.getType() != ChatDetail.TYPE_AUDIO) {
            return;
        }

        AudioInfo audioInfo = AudioInfo.fromJson(chatDetail.getContent());
        Uri uri = Uri.parse(audioInfo.getUri());
        int durationMills = audioInfo.getDuration();
        int duration = durationMills / 1000;
        if (durationMills % 1000 != 0) {
            duration++;
        }
        VoiceMessage vm = VoiceMessage.obtain(uri, duration);
        vm.setExtra(chatDetail.toJson());

        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE, /* Conversation type */
                chatDetail.getToId(), /* targetId */
                vm, /* Message content */
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
