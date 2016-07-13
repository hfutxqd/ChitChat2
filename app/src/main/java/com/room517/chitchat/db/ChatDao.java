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

    /**
     * 判断是否存在任意对话
     * @return 如果没有对话，返回{@code true}，否则返回{@code false}
     */
    public boolean noChats() {
        Cursor cursor = db.query(TableChat.TableName, null, null, null, null, null, null);
        return !thereAreSomeRows(cursor);
    }

    /**
     * 判断是否存在置顶对话
     * @return 如果没有置顶对话，返回{@code true}，否则返回{@code false}
     */
    public boolean noStickyChats() {
        String selection = TableChat.TYPE + "=" + Chat.TYPE_STICKY;
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        return !thereAreSomeRows(cursor);
    }

    /**
     * 判断是否存在普通对话
     * @return 如果没有普通对话，返回{@code true}，否则返回{@code false}
     */
    public boolean noNormalChats() {
        String selection = TableChat.TYPE + "=" + Chat.TYPE_NORMAL;
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        return !thereAreSomeRows(cursor);
    }

    /**
     * 根据用户ID，获得相应的{@link Chat}对象
     * @param userId 用户ID
     * @param loadChatDetails 是否给{@link Chat#mChatDetailsToDisplay}赋值，该操作可能会消耗一定时间，
     *                        如果只是想获得{@link Chat}对象，可以传false
     * @return 相应的{@link Chat}对象
     */
    public Chat getChat(@NonNull String userId, boolean loadChatDetails) {
        String selection = TableChat.USER_ID + "='" + userId + "'";
        Cursor cursor = db.query(TableChat.TableName, null, selection, null, null, null, null);
        Chat chat = null;
        if (cursor.moveToNext()) {
            chat = new Chat(cursor);
            if (loadChatDetails) {
                chat.setChatDetailsToDisplay(getChatDetailsToDisplay(userId));
            }
        }
        cursor.close();
        return chat;
    }

    /**
     * 根据{@link ChatDetail}对象，获得相应的{@link Chat}对象
     * @param chatDetail 已有的聊天记录对象
     * @param loadChatDetails 是否给{@link Chat#mChatDetailsToDisplay}赋值，该操作可能会消耗一定时间，
     *                        如果只是想获得{@link Chat}对象，可以传false
     * @return 相应的{@link Chat}对象
     */
    public Chat getChat(@NonNull ChatDetail chatDetail, boolean loadChatDetails) {
        String fromId = chatDetail.getFromId();
        String userId = fromId;
        if (fromId.equals(App.getMe().getId())) {
            userId = chatDetail.getToId();
        }
        return getChat(userId, loadChatDetails);
    }

    /**
     * @param type {@link Chat}的类型，即{@link Chat#type}
     * @return 所有在数据库中存储的，符合{@param type}类型的 {@link Chat}对象
     */
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

    public ChatDetail getChatDetail(String id) {
        String selection = TableChatDetail.ID + "='" + id + "'";
        Cursor cursor = db.query(TableChatDetail.TableName, null, selection, null,
                null, null, null);
        ChatDetail chatDetail = null;
        if (cursor.moveToFirst()) {
            chatDetail = new ChatDetail(cursor);
        }
        cursor.close();
        return chatDetail;
    }

    /**
     * 获得和相应用户的最后一条聊天记录
     * @param userId 对方的id
     * @return 和该用户的最后一条聊天记录
     */
    public ChatDetail getLastChatDetailToDisplay(@NonNull String userId) {
        String part1 =     TableChatDetail.FROM_ID + "='" + userId + "'"
                + " or " + TableChatDetail.TO_ID   + "='" + userId + "'";
        String part2 = TableChatDetail.TYPE + "<" + ChatDetail.TYPE_CMD_WITHDRAW;

        String selection = "(" + part1 + ") and (" + part2 + ")";
        String orderBy   = TableChatDetail.TIME + " desc";

        Cursor cursor = db.query(TableChatDetail.TableName, null, selection, null,
                null, null, orderBy);
        ChatDetail chatDetail = null;
        if (cursor.moveToFirst()) {
            chatDetail = new ChatDetail(cursor);
        }
        cursor.close();
        return chatDetail;
    }

    public List<ChatDetail> searchChatDetails(String key, boolean exceptImageAudio) {
        List<ChatDetail> chatDetails = new ArrayList<>();
        key = key.replaceAll("'", "''");

        String selection = "(" + TableChatDetail.CONTENT + " like '%" + key + "%')";
        if (exceptImageAudio) { // TODO: 2016/7/13 location here?
            selection += " and " + TableChatDetail.TYPE + " = " + ChatDetail.TYPE_TEXT;
        }

        Cursor cursor = db.query(TableChatDetail.TableName, null, selection,
                null, null, null, null);
        while (cursor.moveToNext()) {
            chatDetails.add(new ChatDetail(cursor));
        }
        cursor.close();

        return chatDetails;
    }

    /**
     * 获得和某个用户的所有聊天记录
     * @param userId 对方的id
     * @return 和该用户的所有聊天记录
     */
    public List<ChatDetail> getChatDetailsToDisplay(@NonNull String userId) {
        List<ChatDetail> chatDetails = new ArrayList<>();

        String part1 =     TableChatDetail.FROM_ID + "='" + userId + "'"
                + " or " + TableChatDetail.TO_ID   + "='" + userId + "'";
        String part2 = TableChatDetail.TYPE + "<" + ChatDetail.TYPE_CMD_WITHDRAW;

        String selection = "(" + part1 + ") and (" + part2 + ")";
        String orderBy   = TableChatDetail.TIME;

        Cursor cursor = db.query(TableChatDetail.TableName, null, selection, null,
                null, null, orderBy);
        while (cursor.moveToNext()) {
            ChatDetail chatDetail = new ChatDetail(cursor);
            chatDetails.add(chatDetail);
        }
        cursor.close();
        return chatDetails;
    }

    /**
     * 插入对话到数据库中
     * @param chat 待插入的对话对象
     * @return 插入成功返回 {@code true}，否则返回 {@code false}
     */
    public boolean insertChat(@NonNull Chat chat) {
        ContentValues values = new ContentValues(2);
        values.put(TableChat.USER_ID, chat.getUserId());
        values.put(TableChat.TYPE,    chat.getType());

        return db.insert(TableChat.TableName, null, values) != -1;
    }

    /**
     * 更新对话的类型（普通、置顶）
     * @param userId 待更新的{@link Chat}对象的id
     * @param newType 新的对话类型
     * @return 更新成功返回 {@code true}，否则返回{@code false}
     */
    public boolean updateChat(@NonNull String userId, @Chat.Type int newType) {
        ContentValues values = new ContentValues(1);
        values.put(TableChat.TYPE, newType);

        String where = TableChat.USER_ID + "='" + userId + "'";
        return db.update(TableChat.TableName, values, where, null) == 1;
    }

    /**
     * 从数据库中删除对话，同时会调用{@link ChatDao#deleteChatDetails(String)}
     * @param userId 待删除的{@link Chat}对象的id
     * @return 删除成功返回 {@code true}，否则返回{@code false}
     */
    public boolean deleteChat(@NonNull String userId) {
        String where = TableChat.USER_ID + "='" + userId + "'";
        deleteChatDetails(userId);
        return db.delete(TableChat.TableName, where, null) == 1;
    }

    /**
     * 向聊天详情中插入新的聊天详情
     * @param chatDetail 待插入的聊天详情对象
     * @return 插入成功新的一行的id，否则返回false
     */
    public long insertChatDetail(@NonNull ChatDetail chatDetail) {
        ContentValues values = new ContentValues();
        values.put(TableChatDetail.ID,      chatDetail.getId());
        values.put(TableChatDetail.FROM_ID, chatDetail.getFromId());
        values.put(TableChatDetail.TO_ID,   chatDetail.getToId());
        values.put(TableChatDetail.TYPE,    chatDetail.getType());
        values.put(TableChatDetail.STATE,   chatDetail.getState());
        values.put(TableChatDetail.CONTENT, chatDetail.getContent());
        values.put(TableChatDetail.TIME,    chatDetail.getTime());

        return db.insert(TableChatDetail.TableName, null, values);
    }

    /**
     * 更新聊天详情的状态（正常、发送中、发送失败），同时也会影响聊天详情的时间，即{@link ChatDetail#time}
     * @param id 待更新的聊天详情的id
     * @param newState 新的状态
     * @return 更新成功返回 {@code true}，否则返回{@code false}
     */
    public boolean updateChatDetailState(String id, @ChatDetail.State int newState) {
        ContentValues values = new ContentValues();
        values.put(TableChatDetail.STATE, newState);
        values.put(TableChatDetail.TIME,  System.currentTimeMillis());

        String where = TableChatDetail.ID + "='" + id + "'";
        return db.update(TableChatDetail.TableName, values, where, null) == 1;
    }

    /**
     * 删除某个聊天记录
     * @param id 待删除的聊天记录的ID
     * @return 删除的行数，在这里如果结果是1说明正确
     */
    public int deleteChatDetail(String id) {
        String where = TableChatDetail.ID + "='" + id + "'";
        return db.delete(TableChatDetail.TableName, where, null);
    }

    /**
     * 删除与某个用户的所有聊天详情，该方法单独调用没有意义
     * @param userId 用户id
     */
    private void deleteChatDetails(@NonNull String userId) {
        String where = TableChatDetail.FROM_ID + " = '" + userId + "' or "
                     + TableChatDetail.TO_ID + " = '"   + userId + "'";
        db.delete(TableChatDetail.TableName, where, null);
    }

    private boolean thereAreSomeRows(@NonNull Cursor cursor) {
        /*
            不直接使用cursor.getCount()的原因是这个方法会对数据表中每一行都执行select来判断是否符合where要求，
            最后才能更新count，这样会大大降低效率，因为我们实际上不关心究竟有多少行，而只在意是不是为空
         */
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
