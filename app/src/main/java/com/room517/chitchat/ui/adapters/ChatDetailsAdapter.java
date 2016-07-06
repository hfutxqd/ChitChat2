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
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.DateTimeUtil;
import com.room517.chitchat.utils.DisplayUtil;

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

        RxBus.get().register(this);
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

    public void notifyStateChanged(String id) {
        notifyItemChanged(mChat.indexOfChatDetail(id));
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_DELETE_MESSAGE) })
    public void onChatDetailDeleted(ChatDetail deleted) {
        int index = mChat.indexOfChatDetail(deleted.getId());
        if (index == -1) {
            return;
        }
        mChat.getChatDetailsToDisplay().remove(index);
        notifyItemRemoved(index);
    }

    @Subscribe(tags = { @Tag(Def.Event.UPDATE_MESSAGE_STATE) })
    public void updateChatDetailState(ChatDetail updated) {
        int index = mChat.indexOfChatDetail(updated.getId());
        if (index == -1) {
            return;
        }
        mChat.getChatDetailsToDisplay().get(index).setState(updated.getState());
        notifyItemChanged(index);
    }

    @Override
    public int getItemViewType(int position) {
        ChatDetail chatDetail = mChat.getChatDetailsToDisplay().get(position);
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

        ChatDetail chatDetail = mChat.getChatDetailsToDisplay().get(position);
        holder.tvContent.setText(chatDetail.getContent());

        if (type == TYPE_ME) {
            holder.cv.setCardBackgroundColor(DisplayUtil.getLightColor(mMe.getColor()));
            @ChatDetail.State int state = chatDetail.getState();
            if (state == ChatDetail.STATE_SENDING
                    || state == ChatDetail.STATE_WITHDRAWING) {
                holder.pbState.setVisibility(View.VISIBLE);
                holder.ivRetry.setVisibility(View.GONE);

                if (state == ChatDetail.STATE_SENDING) {
                    holder.tvTimeState.setText(App.getApp().getString(R.string.sending));
                } else if (state == ChatDetail.STATE_WITHDRAWING) {
                    holder.tvTimeState.setText(App.getApp().getString(R.string.withdrawing));
                }

            } else if (state == ChatDetail.STATE_SEND_FAILED
                    || state == ChatDetail.STATE_WITHDRAW_FAILED) {
                holder.pbState.setVisibility(View.GONE);
                holder.ivRetry.setVisibility(View.VISIBLE);

                if (state == ChatDetail.STATE_SEND_FAILED) {
                    holder.tvTimeState.setText(App.getApp().getString(R.string.error_send_message_failed));
                } else if (state == ChatDetail.STATE_WITHDRAW_FAILED) {
                    holder.tvTimeState.setText(App.getApp().getString(R.string.error_withdraw_failed));
                }

            } else {
                holder.pbState.setVisibility(View.GONE);
                holder.ivRetry.setVisibility(View.GONE);

                holder.tvTimeState.setText(DateTimeUtil.getExactDateTimeString(chatDetail.getTime()));
            }
        } else {
            holder.cv.setCardBackgroundColor(Color.WHITE);
            holder.tvTimeState.setText(DateTimeUtil.getExactDateTimeString(chatDetail.getTime()));
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
        return mChat.getChatDetailsToDisplay().size();
    }

    class ChatDetailHolder extends BaseViewHolder {

        ImageView ivAvatar;
        CardView  cv;
        TextView  tvContent;
        TextView  tvTimeState;

        ProgressBar pbState;
        ImageView ivRetry;

        public ChatDetailHolder(View itemView) {
            super(itemView);
            ivAvatar    = f(R.id.iv_avatar_chat_detail);
            cv          = f(R.id.cv_content_chat_detail);
            tvContent   = f(R.id.tv_content_chat_detail);
            tvTimeState = f(R.id.tv_time_state_chat_detail);

            pbState = f(R.id.pb_state_chat_detail);
            ivRetry = f(R.id.iv_retry_chat_detail);

            cv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    RxBus.get().post(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED,
                            mChat.getChatDetailsToDisplay().get(getAdapterPosition()));
                    return true;
                }
            });

            if (ivRetry != null) {
                ivRetry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        ChatDetail chatDetail = mChat.getChatDetailsToDisplay().get(pos);
                        int state = chatDetail.getState();
                        if (state == ChatDetail.STATE_SEND_FAILED) {
                            sendMessageAgain(chatDetail, pos);
                        } else if (state == ChatDetail.STATE_WITHDRAW_FAILED) {
                            withdrawMessageAgain(chatDetail);
                        }
                    }
                });
            }
        }

        private void sendMessageAgain(ChatDetail chatDetail, int pos) {
            chatDetail.setState(ChatDetail.STATE_SENDING);
            notifyItemChanged(pos);

            /**
             * see {@link com.room517.chitchat.ui.fragments.ChatDetailsFragment#sendTextMessage}
             */
            RxBus.get().post(Def.Event.SEND_MESSAGE, chatDetail);
        }

        private void withdrawMessageAgain(ChatDetail chatDetail) {
            /**
             * see {@link com.room517.chitchat.ui.fragments.ChatDetailsFragment#tryToWithdrawChatDetail}
             */
            RxBus.get().post(Def.Event.WITHDRAW_MESSAGE, chatDetail);
        }
    }

}
