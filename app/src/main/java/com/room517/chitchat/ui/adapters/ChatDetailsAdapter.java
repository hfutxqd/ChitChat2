package com.room517.chitchat.ui.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.DateTimeUtil;
import com.room517.chitchat.utils.DisplayUtil;

import java.util.List;

/**
 * Created by ywwynm on 2016/5/26.
 * 用于显示聊天内容
 */
public class ChatDetailsAdapter extends RecyclerView.Adapter<ChatDetailsAdapter.ChatDetailHolder> {

    private static final int TYPE_ME = 0;
    private static final int TYPE_OTHER = 1;

    private LayoutInflater mInflater;
    private Chat mChat;

    private User mMe;
    private User mOther;

    private Drawable mAvatarMe;
    private Drawable mAvatarOther;

    public ChatDetailsAdapter(Activity activity, Chat chat) {
        mInflater = LayoutInflater.from(activity);
        mChat = chat;

        mMe = App.getMe();
        mAvatarMe = App.getMe().getAvatarDrawable();

        initOther();
    }

    public void initOther() {
        mOther = UserDao.getInstance().getUserById(mChat.getUserId());
        if (mOther != null) {
            mAvatarOther = mOther.getAvatarDrawable();
        }
    }

    public Chat getChat() {
        return mChat;
    }

    public void notifyStateChanged(ChatDetail chatDetail) {
        List<ChatDetail> chatDetails = mChat.getChatDetails();
        final int size = chatDetails.size();
        for (int i = 0; i < size; i++) {
            ChatDetail cd = chatDetails.get(i);
            if (cd.getId() == chatDetail.getId()) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatDetail chatDetail = mChat.getChatDetails().get(position);
        String fromId = chatDetail.getFromId();
        if (fromId.equals(mMe.getId())) {
            return TYPE_ME;
        } else {
            return TYPE_OTHER;
        }
    }

    @Override
    public ChatDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ME) {
            return new ChatDetailHolder(
                    mInflater.inflate(R.layout.rv_chat_detail_me, parent, false));
        } else {
            return new ChatDetailHolder(
                    mInflater.inflate(R.layout.rv_chat_detail_other, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(ChatDetailHolder holder, int position) {
        int type = getItemViewType(position);

        if (type == TYPE_ME) {
            holder.ivAvatar.setImageDrawable(mAvatarMe);
        } else {
            holder.ivAvatar.setImageDrawable(mAvatarOther);
        }

        ChatDetail chatDetail = mChat.getChatDetails().get(position);
        holder.tvContent.setText(chatDetail.getContent());

        if (type == TYPE_ME) {
            holder.cv.setCardBackgroundColor(DisplayUtil.getLightColor(mMe.getColor()));
            int state = chatDetail.getState();
            if (state == ChatDetail.STATE_SENDING) {
                holder.pbState.setVisibility(View.VISIBLE);
                holder.ivRetry.setVisibility(View.GONE);
                holder.tvTime.setText(App.getApp().getString(R.string.sending));
            } else if (state == ChatDetail.STATE_SEND_FAILED) {
                holder.pbState.setVisibility(View.GONE);
                holder.ivRetry.setVisibility(View.VISIBLE);
                holder.tvTime.setText(App.getApp().getString(R.string.error_send_message_failed));
            } else {
                holder.pbState.setVisibility(View.GONE);
                holder.ivRetry.setVisibility(View.GONE);
                holder.tvTime.setText(DateTimeUtil.getExactDateTimeString(chatDetail.getTime()));
            }
        } else {
            holder.cv.setCardBackgroundColor(Color.WHITE);
            holder.tvTime.setText(DateTimeUtil.getExactDateTimeString(chatDetail.getTime()));
        }

        updateMargins(holder, position);
    }

    private void updateMargins(ChatDetailHolder holder, int position) {
        final int count = getItemCount();
        RecyclerView.LayoutParams rlp = (RecyclerView.LayoutParams)
                holder.itemView.getLayoutParams();
        if (position == 0) {
            rlp.topMargin = DisplayUtil.dp2px(8);
            rlp.bottomMargin = 0;
        } else if (position == count - 1) {
            rlp.topMargin = DisplayUtil.dp2px(0);
            rlp.bottomMargin = DisplayUtil.dp2px(8);
        } else {
            rlp.topMargin = 0;
            rlp.bottomMargin = 0;
        }
    }

    @Override
    public int getItemCount() {
        return mChat.getChatDetails().size();
    }

    class ChatDetailHolder extends BaseViewHolder {

        ImageView ivAvatar;
        CardView cv;
        TextView tvContent;
        TextView tvTime;

        ProgressBar pbState;
        ImageView ivRetry;

        public ChatDetailHolder(View itemView) {
            super(itemView);
            ivAvatar = f(R.id.iv_avatar_chat_detail);
            cv = f(R.id.cv_content_chat_detail);
            tvContent = f(R.id.tv_content_chat_detail);
            tvTime = f(R.id.tv_time_chat_detail);

            pbState = f(R.id.pb_state_chat_detail);
            ivRetry = f(R.id.iv_retry_chat_detail);

            cv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    RxBus.get().post(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED,
                            mChat.getChatDetails().get(getAdapterPosition()));
                    return true;
                }
            });

            if (ivRetry != null) {
                ivRetry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        ChatDetail chatDetail = mChat.getChatDetails().get(pos);
                        resendMessage(chatDetail, pos);
                    }
                });
            }
        }

        private void resendMessage(ChatDetail chatDetail, int pos) {
            chatDetail.setState(ChatDetail.STATE_SENDING);
            notifyItemChanged(pos);

            RxBus.get().post(Def.Event.SEND_MESSAGE, chatDetail);
        }
    }

}
