package com.room517.chitchat.receivers;

import android.content.Context;

import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.NotificationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.JsonUtil;

import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;
import retrofit2.Retrofit;

// TODO: 2016/6/5 don't know if this is useful
public class PushReceiver extends PushMessageReceiver {

    public PushReceiver() {
    }

    @Override
    public boolean onNotificationMessageArrived(
            final Context context, final PushNotificationMessage message) {
        String fromId = message.getSenderId();
        //如果是从系统发来的消息,断定其为评论通知
        if(fromId.equals(Def.Constant.SYSTEM_ID)){
            if (App.shouldNotifyMessage(message.getSenderId())) {
                String exploreId = JsonUtil.getParam(message.getPushContent(), "explore_id").getAsString();
                String userId = JsonUtil.getParam(message.getPushContent(), "user_id").getAsString();
                String content = JsonUtil.getParam(message.getPushContent(), "content").getAsString();
                NotificationHelper.notifyComment(context, exploreId, userId, content);
            }
            return true;
        }
        final UserDao userDao = UserDao.getInstance();
        if (userDao.getUserById(fromId) == null) {
            /*
               数据库中还没有该User，插入新的Chat或ChatDetail都会失败（因为外键的缘故），
               所以需要先从服务器获取该User的完整信息并插入到本地数据库
            */
            Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
            UserService service = retrofit.create(UserService.class);
            RxHelper.ioMain(service.getUserById(fromId), new SimpleObserver<User>() {
                @Override
                public void onNext(User user) {
                    userDao.insert(user);
                    insertChatDetailAndNotifyUser(context, new ChatDetail(message));
                }
            });
        } else if (App.shouldNotifyMessage(message.getSenderId())) {
            NotificationHelper.notifyMessage(context, message.getSenderId(), message.getPushContent());
        }

        return true;
    }

    @Override
    public boolean onNotificationMessageClicked(
            Context context, PushNotificationMessage pushNotificationMessage) {
        return false;
    }

    private void insertChatDetailAndNotifyUser(Context context, ChatDetail chatDetail) {
        String fromId = chatDetail.getFromId();
        ChatDao chatDao = ChatDao.getInstance();
        if (chatDao.getChat(fromId, false) == null) {
            Chat chat = new Chat(fromId, Chat.TYPE_NORMAL);
            chatDao.insertChat(chat);
        }
        chatDao.insertChatDetail(chatDetail);

        if (App.shouldNotifyMessage(fromId)) {
            NotificationHelper.notifyMessage(context, fromId, chatDetail.getContent());
        }
    }

}
