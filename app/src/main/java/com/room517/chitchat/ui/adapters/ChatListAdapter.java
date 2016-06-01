package com.room517.chitchat.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywwynm on 2016/5/30.
 * 用于主界面显示聊天列表
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> {

    @Chat.Type
    private int mType;

    private LayoutInflater mInflater;

    private List<Chat> mChats;
    private List<User> mUsers;
    private List<ChatDetail> mLastChatDetails;
    private List<Integer> mUnreadCounts;

    public ChatListAdapter(Activity activity, List<Chat> chats, @Chat.Type int type) {
        mInflater = LayoutInflater.from(activity);

        int size = chats.size();
        mUsers  = new ArrayList<>(size);
        mLastChatDetails = new ArrayList<>(size);
        mUnreadCounts = new ArrayList<>(size);
        mChats = chats;

        mType = type;

        UserDao userDao = UserDao.getInstance();
        ChatDao chatDao = ChatDao.getInstance();
        for (Chat chat : mChats) {
            String userId = chat.getUserId();
            mUsers.add(userDao.getUserById(userId));
            mLastChatDetails.add(chatDao.getLastChatDetail(userId));
            mUnreadCounts.add(0);
        }

        RxBus.get().register(this); // TODO: 2016/5/30 更改adapter、删除时都要unregister RxBus
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_RECEIVE_MESSAGE) })
    public void onMessageReceived(ChatDetail chatDetail) {
        onNewChatDetailAdded(chatDetail, true);
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_SEND_MESSAGE) })
    public void onMessageSent(ChatDetail chatDetail) {
        onNewChatDetailAdded(chatDetail, false);
    }

    private void onNewChatDetailAdded(ChatDetail chatDetail, boolean receive) {
        Chat chat = ChatDao.getInstance().getChat(chatDetail, false);
        if (chat.getType() != mType) {
            return;
        }

        String userId = receive ? chatDetail.getFromId() : chatDetail.getToId();
        User user = UserDao.getInstance().getUserById(userId);
        int posBefore = getRelatedChatDetailPosition(userId);
        if (posBefore != -1) {
            mUsers.remove(posBefore);
            mChats.remove(posBefore);
            mLastChatDetails.remove(posBefore);
        }
        mUsers.add(0, user);
        mChats.add(0, chat);
        mLastChatDetails.add(0, chatDetail);

        // 更新未读计数
        if (receive) {
            if (posBefore != -1) {
                int unread = mUnreadCounts.remove(posBefore);
                mUnreadCounts.add(0, unread + 1);
            } else {
                mUnreadCounts.add(0, 1);
            }
        } else {
            if (posBefore != -1) {
                mUnreadCounts.remove(posBefore);
            }
            mUnreadCounts.add(0, 0);
        }

        if (posBefore != -1) {
            if (posBefore == 0) {
                notifyItemChanged(0);
            } else {
                notifyItemMoved(posBefore, 0);
                notifyItemChanged(0);
            }
        } else {
            notifyItemInserted(0);
        }
    }

    private int getRelatedChatDetailPosition(String userId) {
        final int size = mLastChatDetails.size();
        for (int i = 0; i < size; i++) {
            ChatDetail chatDetail = mLastChatDetails.get(i);
            if (userId.equals(chatDetail.getFromId()) || userId.equals(chatDetail.getToId())) {
                return i;
            }
        }
        return -1;
    }

    public List<Integer> getUnreadCounts() {
        return mUnreadCounts;
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatHolder(mInflater.inflate(R.layout.rv_chat_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        User user = mUsers.get(position);
        holder.ivAvatar.setImageDrawable(user.getAvatarDrawable());
        holder.tvName.setText(user.getName());

        ChatDetail chatDetail = mLastChatDetails.get(position);
        holder.tvContent.setText(chatDetail.getContent() + "----" + mUnreadCounts.get(position));
        holder.tvTime.setText(DateTimeUtil.getShortDateTimeString(chatDetail.getTime()));

        if (position != getItemCount() - 1) {
            holder.separator.setVisibility(View.VISIBLE);
        } else {
            holder.separator.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    class ChatHolder extends BaseViewHolder {

        ImageView ivAvatar;
        TextView  tvName;
        TextView  tvContent;
        TextView  tvTime;
        View      separator;

        public ChatHolder(View itemView) {
            super(itemView);

            ivAvatar  = f(R.id.iv_avatar_chat_list);
            tvName    = f(R.id.tv_name_chat_list);
            tvContent = f(R.id.tv_content_chat_list);
            tvTime    = f(R.id.tv_time_chat_list);
            separator = f(R.id.view_separator);

            f(R.id.rl_chat_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    mUnreadCounts.set(pos, 0);
                    notifyItemChanged(pos);
                    RxBus.get().post(Def.Event.START_CHAT, mUsers.get(pos));
                }
            });
        }
    }

}
