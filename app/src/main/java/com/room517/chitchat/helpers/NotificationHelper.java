package com.room517.chitchat.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.ExploreDetailActivity;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.utils.BitmapUtil;
import com.room517.chitchat.utils.DeviceUtil;

import java.util.HashMap;


/**
 * Created by ywwynm on 2016/6/5.
 * 通知的帮助类
 */
public class NotificationHelper {

    private static HashMap<String, Integer> unreadNotifications = new HashMap<>();

    private static HashMap<String, Integer> unreadComments = new HashMap<>();

    /**
     * 对接收到的消息进行通知
     * 如果只有一条消息，内容就是消息正文；
     * 如果用户没有查看该消息，之后又收到了新消息，那么内容就是“x 条未读消息”
     *
     * @param userId  接收到的消息的发送者的id
     * @param content 接收到的消息的内容
     */
    public static void notifyMessage(Context context, String userId, String content) {
        Integer count = unreadNotifications.get(userId);
        unreadNotifications.put(userId, count == null ? 1 : count + 1);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        User user = UserDao.getInstance().getUserById(userId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        BitmapUtil.drawableToBitmap(
                                user.getAvatarDrawable(),
                                context.getResources().getDimensionPixelSize(
                                        android.R.dimen.notification_large_icon_width)))
                .setContentTitle(user.getName())
                .setGroup(userId)
                .setAutoCancel(true);

        if (DeviceUtil.hasJellyBeanApi()) {
            // 主要用于启用heads-up的通知形式，因为是即时通讯软件，所以优先级也应该比较高
            builder.setPriority(Notification.PRIORITY_MAX);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Def.Key.USER, user);
        builder.setContentIntent(PendingIntent.getActivity(
                context, userId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT));

        count = unreadNotifications.get(userId);
        if (count == 1) {
            builder.setContentText(content);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        } else {
            builder.setContentText(count + " " + context.getString(R.string.unread_messages));
        }

        // 直接使用userId对象的hash值作为通知的标识符，可以提供许多便利
        manager.notify(userId.hashCode(), builder.build());
    }

    public static Integer getUnreadCount(String userId) {
        return unreadNotifications.get(userId);
    }

    public static void putUnreadCount(String userId, int count) {
        unreadNotifications.put(userId, count);
    }


    /**
     * 对接收到的评论进行通知
     * 如果该动态只有一条评论，内容就是正文；
     * 如果用户没有查看该评论，之后又收到了新评论，那么内容就是“x 个评论”
     *
     * @param exploreId 被评论动态的id
     * @param userId    评论者的id
     * @param content   评论内容
     */
    public static void notifyComment(Context context, String exploreId, String userId, String content) {
        Log.i("notifyComment", "notification is sending...");
        Integer count = unreadComments.get(exploreId);
        unreadComments.put(exploreId, count == null ? 1 : count + 1);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        User user = UserDao.getInstance().getUserById(userId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        BitmapUtil.drawableToBitmap(
                                user.getAvatarDrawable(),
                                context.getResources().getDimensionPixelSize(
                                        android.R.dimen.notification_large_icon_width)))
                .setContentTitle(user.getName() + context.getString(R.string.new_comment))
                .setGroup(exploreId)
                .setAutoCancel(true);
        Log.i("notifyComment", "notification is sending...2");
        if (DeviceUtil.hasJellyBeanApi()) {
            builder.setPriority(Notification.PRIORITY_MAX);
        }

        Intent intent = new Intent(context, ExploreDetailActivity.class);
        intent.putExtra(Def.Key.EXPLORE_ID, exploreId);
        builder.setContentIntent(PendingIntent.getActivity(
                context, exploreId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT));

        count = unreadComments.get(exploreId);
        if (count == 1) {
            builder.setContentText(content);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        } else {
            builder.setContentText(count + " " + context.getString(R.string.unread_comments));
        }
        manager.notify(exploreId.hashCode(), builder.build());
        Log.i("notifyComment", "notification sended.");
    }

    public static void clearUnreadCommentsCount(String explore_id) {
        unreadComments.put(explore_id, 0);
    }

}
