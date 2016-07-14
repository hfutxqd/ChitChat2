package com.room517.chitchat.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.AMapLocationHelper;
import com.room517.chitchat.model.AudioInfo;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.LocationInforActivity;
import com.room517.chitchat.utils.DateTimeUtil;
import com.room517.chitchat.utils.DisplayUtil;
import com.room517.chitchat.utils.StringUtil;
import com.ywwynm.emoji.EmojiTextView;

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

    private Drawable mAvatarMe;
    private Drawable mAvatarOther;

    private MediaPlayer mPlayer;
    private String mPlayingId;

    public ChatDetailsAdapter(Activity activity, Chat chat) {
        mInflater = LayoutInflater.from(activity);
        mChat = chat;

        mMe = App.getMe();
        mAvatarMe = App.getMe().getAvatarDrawable();

        initOther();

        RxBus.get().register(this);
    }

    public void initOther() {
        User other = UserDao.getInstance().getUserById(mChat.getUserId());
        if (other != null) {
            mAvatarOther = other.getAvatarDrawable();
        }
    }

    public Chat getChat() {
        return mChat;
    }

    public void notifyStateChanged(String id) {
        notifyItemChanged(mChat.indexOfChatDetail(id));
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

        updateCardUiForText(holder, chatDetail);
        updateCardUiForImage(holder, chatDetail);
        updateCardUiForAudio(holder, chatDetail);
        updateCardForLocation(holder, chatDetail);

        if (type == TYPE_ME) {
            holder.cv.setCardBackgroundColor(DisplayUtil.getLightColor(mMe.getColor()));
            updateCardUiForState(holder, chatDetail);
        } else {
            holder.cv.setCardBackgroundColor(Color.WHITE);
            holder.tvTimeState.setText(DateTimeUtil.getExactDateTimeString(chatDetail.getTime()));
        }

        updateMargins(holder, position);
    }

    private void updateCardUiForText(ChatDetailHolder holder, ChatDetail chatDetail) {
        if (chatDetail.getType() == ChatDetail.TYPE_TEXT) {
            holder.tvContent.setVisibility(View.VISIBLE);

            String content = chatDetail.getContent();
            int count = StringUtil.countOf(content, '!', '！');
            int textSize = 16;
            int emojiSize = DisplayUtil.dp2px(32);
            for (int i = 0; i < count; i++) {
                textSize += 4;
                emojiSize += DisplayUtil.dp2px(4);
            }
            if (textSize > 40) {
                textSize = 40;
                emojiSize = DisplayUtil.dp2px(56);
            }
            holder.tvContent.setTextSize(textSize);
            holder.tvContent.setEmojiSize(emojiSize);
            holder.tvContent.setText(chatDetail.getContent());
        } else {
            holder.tvContent.setVisibility(View.GONE);
        }
    }

    private void updateCardUiForImage(ChatDetailHolder holder, ChatDetail chatDetail) {
        if (chatDetail.getType() == ChatDetail.TYPE_IMAGE) {
            holder.ivImage.setVisibility(View.VISIBLE);
            String decodedUriStr = Uri.decode(chatDetail.getContent());
            ImageLoader.getInstance().displayImage(decodedUriStr, holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }
    }

    private void updateCardUiForAudio(ChatDetailHolder holder, ChatDetail chatDetail) {
        if (chatDetail.getType() == ChatDetail.TYPE_AUDIO) {
            holder.llAudio.setVisibility(View.VISIBLE);
            AudioInfo audioInfo = AudioInfo.fromJson(chatDetail.getContent());

            int duration = audioInfo.getDuration();
            holder.tvAudioDuration.setText(DateTimeUtil.getDurationString(duration));

            int aver = audioInfo.getAverageDecibel();
            // 假设50分贝为最小分贝数，50 -> 16sp, 20dp
            // 80分贝为最大分贝数，80 -> 40sp, 50dp
            if (aver <= 50) {
                updateIvAudioStateSize(holder.ivAudioState, 20);
                holder.tvAudioDuration.setTextSize(16);
            } else if (aver >= 80) {
                updateIvAudioStateSize(holder.ivAudioState, 50);
                holder.tvAudioDuration.setTextSize(40);
            } else {
                int ivSize = aver - 30;
                updateIvAudioStateSize(holder.ivAudioState, ivSize);

                float textSize = 1.2f * aver - 46;
                holder.tvAudioDuration.setTextSize(textSize);
            }


            if (!chatDetail.getId().equals(mPlayingId)) {
                holder.ivAudioState.setImageResource(R.drawable.act_play_black_chat_detail);
            } else {
                holder.ivAudioState.setImageResource(R.drawable.act_stop_black_chat_detail);
            }
        } else {
            holder.llAudio.setVisibility(View.GONE);
        }
    }

    private void updateIvAudioStateSize(ImageView iv, int dp) {
        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) iv.getLayoutParams();
        llp.width = DisplayUtil.dp2px(dp);
        llp.height = llp.width;
        iv.requestLayout();
    }

    private void updateCardForLocation(ChatDetailHolder holder, ChatDetail chatDetail) {
        if (chatDetail.getType() == ChatDetail.TYPE_LOCATION) {
            holder.llLocation.setVisibility(View.VISIBLE);
            AMapLocation location = AMapLocationHelper.getLocationFromString(
                    chatDetail.getContent());
            if (location == null) {
                throw new IllegalStateException(
                        "A location is null in ChatDetailsAdapter.");
            }
            holder.tvLocation.setText(location.getPoiName());
            holder.tvAddress.setText(location.getAddress());
        } else {
            holder.llLocation.setVisibility(View.GONE);
        }
    }

    private void updateCardUiForState(ChatDetailHolder holder, ChatDetail chatDetail) {
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

    public String getPlayingId() {
        return mPlayingId;
    }

    public void stopPlaying() {
        String playingId = mPlayingId;
        releaseAudioPlayer();
        mPlayingId = null;

        int index = mChat.indexOfChatDetail(playingId);
        if (index != -1) {
            notifyItemChanged(mChat.indexOfChatDetail(playingId));
        }
    }

    public void releaseAudioPlayer() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    class ChatDetailHolder extends BaseViewHolder {

        ImageView ivAvatar;
        CardView  cv;

        EmojiTextView tvContent;

        ImageView ivImage;

        LinearLayout llAudio;
        ImageView    ivAudioState;
        TextView     tvAudioDuration;

        LinearLayout llLocation;
        TextView     tvLocation;
        TextView     tvAddress;

        TextView  tvTimeState;

        ProgressBar pbState;
        ImageView ivRetry;

        public ChatDetailHolder(View itemView) {
            super(itemView);
            ivAvatar = f(R.id.iv_avatar_chat_detail);
            cv       = f(R.id.cv_content_chat_detail);

            tvContent = f(R.id.tv_content_chat_detail);

            ivImage   = f(R.id.iv_image_chat_detail);

            llAudio         = f(R.id.ll_audio_chat_detail);
            ivAudioState    = f(R.id.iv_audio_state_chat_detail);
            tvAudioDuration = f(R.id.tv_audio_duration_chat_detail);

            llLocation = f(R.id.ll_location_chat_detail);
            tvLocation = f(R.id.tv_location_chat_detail);
            tvAddress  = f(R.id.tv_address_chat_detail);

            tvTimeState = f(R.id.tv_time_state_chat_detail);

            pbState = f(R.id.pb_state_chat_detail);
            ivRetry = f(R.id.iv_retry_chat_detail);

            setupEvents();
        }

        private void setupEvents() {
            setupCardEvents();

            if (ivRetry != null) {
                setupRetryEvents();
            }
        }

        private void setupCardEvents() {
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    ChatDetail chatDetail = mChat.getChatDetailsToDisplay().get(pos);
                    @ChatDetail.Type int type = chatDetail.getType();
                    if (type == ChatDetail.TYPE_IMAGE) {
                        onImageClicked(chatDetail, ivImage);
                    } else if (type == ChatDetail.TYPE_AUDIO) {
                        onAudioClicked(chatDetail);
                    } else if (type == ChatDetail.TYPE_LOCATION) {
                        onLocationClicked(chatDetail);
                    }
                }
            });

            cv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    RxBus.get().post(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED,
                            mChat.getChatDetailsToDisplay().get(getAdapterPosition()));
                    return true;
                }
            });
        }

        private void onImageClicked(ChatDetail chatDetail, View v) {
            Def.Event.CheckImage checkImage = new Def.Event.CheckImage();
            checkImage.uri  = chatDetail.getContent();
            checkImage.view = v;
            RxBus.get().post(Def.Event.ON_IMAGE_CHAT_DETAIL_CLICKED, checkImage);
        }

        private void onAudioClicked(ChatDetail chatDetail) {
            String playingId = mPlayingId;
            stopPlaying();

            if (!chatDetail.getId().equals(playingId)) {
                mPlayingId = chatDetail.getId();

                AudioInfo audioInfo = AudioInfo.fromJson(chatDetail.getContent());
                String uriStr = audioInfo.getUri();
                Uri uri = Uri.parse(Uri.decode(uriStr));

                mPlayer = MediaPlayer.create(App.getApp(), uri);
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopPlaying();
                    }
                });
                mPlayer.start();
            }

            notifyItemChanged(getAdapterPosition());
        }

        private void onLocationClicked(ChatDetail chatDetail) {
            AMapLocation location = AMapLocationHelper.getLocationFromString(
                    chatDetail.getContent());
            if (location == null) {
                return;
            }

            Intent intent = new Intent(cv.getContext(), LocationInforActivity.class);
            intent.putExtra(LocationInforActivity.ARG_TITLE, location.getPoiName());
            intent.putExtra(LocationInforActivity.ARG_LONGITUDE, location.getLongitude());
            intent.putExtra(LocationInforActivity.ARG_LATITUDE, location.getLatitude());
            cv.getContext().startActivity(intent);
        }

        private void setupRetryEvents() {
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

        private void sendMessageAgain(ChatDetail chatDetail, int pos) {
            chatDetail.setState(ChatDetail.STATE_SENDING);
            notifyItemChanged(pos);

            /**
             * see {@link com.room517.chitchat.ui.fragments.ChatDetailsFragment#sendMessage}
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





    // ------------------------------ Event subscribers ------------------------------ //



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

}
