package com.room517.chitchat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.room517.chitchat.App;
import com.room517.chitchat.helpers.DBHelper;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;

import java.util.ArrayList;
import java.util.List;

import static com.room517.chitchat.Def.DB.TableChat;
import static com.room517.chitchat.Def.DB.TableChatDetail;

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

    public boolean noChats() {
        Cursor cursor = db.query(TableChat.TableName, null, null, null, null, null, null);
        return !thereAreSomeRows(cursor);
    }

    public boolean noStickyChats() {
        String selection = TableChat.TYPE + "=" + Chat.TYPE_STICKY;
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        return !thereAreSomeRows(cursor);
    }

    public boolean noNormalChats() {
        String selection = TableChat.TYPE + "=" + Chat.TYPE_NORMAL;
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        return !thereAreSomeRows(cursor);
    }

    public Chat getChat(@NonNull String userId, boolean loadChatDetails) {
        String selection = TableChat.USER_ID + "='" + userId + "'";
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        Chat chat = null;
        if (cursor.moveToNext()) {
            chat = new Chat(cursor);
            if (loadChatDetails) {
                chat.setChatDetails(getChatDetails(userId));
            }
        }
        cursor.close();
        return chat;
    }

    public Chat getChat(@NonNull ChatDetail chatDetail, boolean loadChatDetails) {
        String fromId = chatDetail.getFromId();
        String userId = fromId;
        if (fromId.equals(App.getMe().getId())) {
            userId = chatDetail.getToId();
        }
        return getChat(userId, loadChatDetails);
    }

    public List<Chat> getChats(@Chat.Type int type) {
        List<Chat> chats = new ArrayList<>();
        String selection = TableChat.TYPE + "=" + type;
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        while (cursor.moveToNext()) {
            chats.add(new Chat(cursor));
        }
        cursor.close();
        return chats;
    }

    public ChatDetail getLastChatDetail(@NonNull String userId) {
        String selection = TableChatDetail.FROM_ID + "='" + userId + "'"
                + " or " + TableChatDetail.TO_ID   + "='" + userId + "'";
        String orderBy   = TableChatDetail.TIME + " desc";
        Cursor cursor = db.query(TableChatDetail.TableName, null, selection, null,
                null, null, orderBy);
        ChatDetail chatDetail = null;
        if (cursor.moveToNext()) {
            chatDetail = new ChatDetail(cursor);
        }
        cursor.close();
        return chatDetail;
    }

    public List<ChatDetail> getChatDetails(@NonNull String userId) {
        List<ChatDetail> chatDetails = new ArrayList<>();
        String selection = TableChatDetail.FROM_ID + "='" + userId + "'"
                + " or " + TableChatDetail.TO_ID   + "='" + userId + "'";
        String orderBy   = TableChatDetail.TIME;
        Cursor cursor = db.query(TableChatDetail.TableName, null, selection, null,
                null, null, orderBy);
        while (cursor.moveToNext()) {
            chatDetails.add(new ChatDetail(cursor));
        }
        cursor.close();
        return chatDetails;
    }

    public synchronized long getNewChatDetailId() {
        String sql = "select max(" + TableChatDetail.ID + ") from " + TableChatDetail.TableName;
        Cursor cursor = db.rawQuery(sql, null);
        long id = -1;
        if (cursor.moveToNext()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id + 1;
    }

    public boolean insertChat(@NonNull Chat chat) {
        ContentValues values = new ContentValues(2);
        values.put(TableChat.USER_ID, chat.getUserId());
        values.put(TableChat.TYPE,    chat.getType());

        return db.insert(TableChat.TableName, null, values) != -1;
    }

    public boolean updateChat(@NonNull String userId, @Chat.Type int newType) {
        ContentValues values = new ContentValues(1);
        values.put(TableChat.TYPE, newType);

        String where = TableChat.USER_ID + "='" + userId + "'";
        return db.update(TableChat.TableName, values, where, null) == 1;
    }

    public boolean deleteChat(@NonNull String userId) {
        String where = TableChat.USER_ID + "='" + userId + "'";
        deleteChatDetails(userId);
        return db.delete(TableChat.TableName, where, null) == 1;
    }

    // TODO: 2016/6/4 test method, should be deleted in release version
    public void updateChatsToNormal() {
        db.execSQL("update chat set type=0 where type=1");
    }

    public boolean insertChatDetail(@NonNull ChatDetail chatDetail) {
        ContentValues values = new ContentValues();
        //values.put(TableChatDetail.ID,    chatDetail.getId());
        values.put(TableChatDetail.FROM_ID, chatDetail.getFromId());
        values.put(TableChatDetail.TO_ID,   chatDetail.getToId());
        values.put(TableChatDetail.STATE,   chatDetail.getState());
        values.put(TableChatDetail.CONTENT, chatDetail.getContent());
        values.put(TableChatDetail.TIME,    chatDetail.getTime());

        return db.insert(TableChatDetail.TableName, null, values) != -1;
    }

    public boolean updateChatDetailState(long id, @ChatDetail.State int newState) {
        ContentValues values = new ContentValues();
        values.put(TableChatDetail.STATE, newState);
        values.put(TableChatDetail.TIME,  System.currentTimeMillis());

        String where = TableChatDetail.ID + "=" + id;
        return db.update(TableChatDetail.TableName, values, where, null) == 1;
    }

    public void deleteChatDetails(@NonNull String userId) {
        String where = TableChatDetail.FROM_ID + " = '" + userId + "' or "
                + TableChatDetail.TO_ID + " = '" + userId + "'";
        db.delete(TableChatDetail.TableName, where, null);
    }

    private boolean thereAreSomeRows(@NonNull Cursor cursor) {
        int count = 0;
        while (cursor.moveToNext()) {
            count++;
            if (count == 1) {
                break;
            }
        }
        cursor.close();
        return count != 0;
    }

}
