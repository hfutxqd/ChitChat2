package com.room517.chitchat;

import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;

/**
 * Created by ywwynm on 2016/5/14.
 * "侃侃"应用里的定义、常量
 */
public class Def {

    private Def() {}

    public static class Constant {

        public static final String VALID = "valid";
        public static final String COMMENT_SYSTEM_ID = "10000";//系统的用户账户id

        public static final String SUCCESS = "success";
        public static final String FAILED  = "failed";

    }

    public static class Meta {

        public static final String APP_NAME = "chitchat";
        public static final String APP_DIR  =
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.room517.chitchat";
        public static final int APP_PURPLE = ContextCompat.getColor(
                App.getApp(), R.color.app_purple);

        public static final String PREFERENCE_META     = "chitchat_meta";
        public static final String PREFERENCE_USER_ME  = "user_me";

    }

    public static class Key {

        private static final String K = BuildConfig.APPLICATION_ID + ".key.";

        public static final String USER                = K + "user";
        public static final String USERS               = K + "users";
        public static final String CHAT                = K + "chat";
        public static final String CHAT_DETAIL         = K + "chat_detail";
        public static final String CHAT_DETAIL_SCROLL  = K + "chat_detail_scroll";
        public static final String UNREAD_COUNT        = K + "unread_count";
        public static final String COLOR               = K + "color";

        public static final String EXPLORE_ID          = K + "explore_id";

        public static class PrefMeta {

        }

        public static class PrefUserMe {

            public static final String ID          = "id";
            public static final String NAME        = "name";
            public static final String SEX         = "sex";
            public static final String AVATAR      = "avatar";
            public static final String TAG         = "tag";
            public static final String LONGITUDE   = "longitude";
            public static final String LATITUDE    = "latitude";
            public static final String CREATE_TIME = "createTime";

        }

        public static class PrefSettings {

            public static final String CAN_WITHDRAW = "can_withdraw";

        }

    }

    public static class Event {

        public static final String START_CHAT                   = "start_chat";
        public static final String PREPARE_FOR_FRAGMENT         = "prepare_for_fragment";
        public static final String BACK_FROM_FRAGMENT           = "back_from_fragment";
        public static final String BACK_FROM_DIALOG             = "back_from_dialog";

        public static final String SEND_MESSAGE                 = "send_message";
        public static final String WITHDRAW_MESSAGE             = "withdraw_message";
        public static final String UPDATE_MESSAGE_STATE         = "update_message_state";
        public static final String ON_DELETE_MESSAGE            = "on_delete_message";
        public static final String ON_SEND_MESSAGE              = "on_send_message";
        public static final String ON_RECEIVE_MESSAGE           = "on_receive_message";

        public static final String CLEAR_UNREAD                 = "clear_unread";
        public static final String CLEAR_NOTIFICATIONS          = "clear_notifications";

        public static final String ON_CHAT_LIST_LONG_CLICKED    = "on_chat_list_long_clicked";
        public static final String ON_IMAGE_CHAT_DETAIL_CLICKED = "on_chat_detail_clicked";
        public static final String ON_CHAT_DETAIL_LONG_CLICKED  = "on_chat_detail_long_clicked";

        public static final String ON_ACTIONBAR_CLICKED         = "on_actionbar_clicked";

        public static final String CHECK_USER_DETAIL            = "check_user_detail";

        public static final String TAKE_PHOTO                   = "take_photo";
        public static final String PICK_IMAGE                   = "pick_image";
        public static final String RECORD_AUDIO                 = "record_audio";
        public static final String AUDIO_RECORDED               = "audio_recorded";
        public static final String LOCATE_ME                    = "locate_me";

        public static final String ON_EXPLORE_SELF_ICON_CLICKED = "on_explore_self_icon_clicked";
        public static final String SHOW_FAB_FROM_BOTTOM         = "show_fab_from_bottom";
        public static final String HIDE_FAB_TO_BOTTOM           = "hide_fab_to_bottom";

        public static final String SEARCH                       = "search";
        public static final String ON_COMPRESS_IMAGE_COMPLETE   = "on_compress_image_complete";
        public static final String ON_FILE_UPLOAD_COMPLETE      = "on_file_upload_complete";
        public static final String ON_FILE_UPLOAD_FAIL          = "on_file_upload_fail";
        public static final String ON_SET_COVER_SUCCESS         = "on_set_cover_success";
        public static final String ON_SET_COVER_FAIL            = "on_set_cover_fail";

        public static class CheckImage {
            public String uri;
            public View view;
        }

        public static class StartChat {
            public User user;
            public ChatDetail chatDetailToScroll;
            public ChatDetail chatDetailToForward;
        }

    }

    public static class Network {

        public static final String BASE_URL = "http://chitchat.lichengbo.cn/";
        public static final String EXPLORE_BASE_URL = "http://139.129.53.121/";
        public static final String EXPLORE_UPLOAD_URL = "http://139.129.53.121/?a=upload";
        public static final String SUCCESS = "success";
        public static final String ERROR   = "error";

        public static final String STATUS   = "status";
        public static final String TOKEN    = "token";
        public static final String TIME     = "time";
        public static final String DISTANCE = "distance";

    }

    public static class DB {

        public static final String NAME = "chitchat.db";
        public static final int VERSION = 2;

        public static class TableUser {

            public static final String TableName = "user";

            public static final String ID          = "id";
            public static final String NAME        = "name";
            public static final String SEX         = "sex";
            public static final String AVATAR      = "avatar";
            public static final String TAG         = "tag";
            public static final String LONGITUDE   = "longitude";
            public static final String LATITUDE    = "latitude";
            public static final String CREATE_TIME = "createTime";
            public static final String COVER_URL   = "cover_url";

        }

        public static class TableChat {

            public static final String TableName = "chat";

            public static final String USER_ID = "uid";
            public static final String TYPE    = "type";

        }

        public static class TableChatDetail {

            public static final String TableName = "chat_detail";

            public static final String ID      = "id";
            public static final String FROM_ID = "from_id";
            public static final String TO_ID   = "to_id";
            public static final String TYPE    = "type";
            public static final String STATE   = "state";
            public static final String CONTENT = "content";
            public static final String TIME    = "time";

        }

    }

    public static class Request {

        public static final int PERMISSION_LOCATION = 0;

        public static final int TAKE_PHOTO          = 0;
        public static final int PICK_IMAGE          = 1;
        public static final int RECORD_AUDIO        = 1;

    }

}
