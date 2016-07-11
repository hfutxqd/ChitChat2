package com.room517.chitchat.ui.fragments;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.RongHelper;
import com.room517.chitchat.model.AudioInfo;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.simpleinterface.SimpleTextWatcher;
import com.room517.chitchat.ui.activities.ImageViewerActivity;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.activities.UserActivity;
import com.room517.chitchat.ui.adapters.ChatDetailsAdapter;
import com.room517.chitchat.ui.dialogs.AlertDialog;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;
import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.DisplayUtil;
import com.room517.chitchat.utils.KeyboardUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by ywwynm on 2016/5/25.
 * 显示聊天详情的fragment
 */
public class ChatDetailsFragment extends BaseFragment {

    public static ChatDetailsFragment newInstance(Bundle args) {
        ChatDetailsFragment fragment = new ChatDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MainActivity mActivity;

    private User mOther;
    private Chat mChat;

    private RecyclerView mRecyclerView;
    private ChatDetailsAdapter mAdapter;

    private EditText mEtContent;
    private ImageView mIvEmoji;
    private ImageView mIvSendMsgAddAtcm;

    // 控制“发送及添加附件”按钮，如果为true，则显示发送图标，否则为添加附件图标
    private boolean mShouldShowAsSendMessage = false;

    private boolean mScrollByUser = false;

    private FrameLayout mContainerBottom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        App.setWrChatDetails(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.setWrChatDetails(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_chat_detail, menu);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeCallbacks();

        Bus rxBus = RxBus.get();
        rxBus.post(Def.Event.CLEAR_UNREAD, mOther);
        if (App.getChatListReference() != null) {
            rxBus.post(Def.Event.CLEAR_NOTIFICATIONS, new Object());
        }

        if (mShouldBackFromFragment) {
            rxBus.post(Def.Event.BACK_FROM_FRAGMENT, new Object());
        }

        App.setWrChatDetails(null);
        rxBus.unregister(mAdapter);
        rxBus.unregister(this);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chat_details;
    }

    public Chat getChat() {
        return mChat;
    }

    @Override
    protected void beforeInit() {
        App.setWrChatDetails(this);
        RxBus.get().register(this);
    }

    @Override
    protected void initMember() {
        mActivity = (MainActivity) getActivity();

        Bundle args = getArguments();
        mOther = args.getParcelable(Def.Key.USER);
        if (mOther != null) {
            String userId = mOther.getId();
            mChat = ChatDao.getInstance().getChat(userId, true);
            if (mChat == null) {
                mChat = new Chat(userId, Chat.TYPE_NORMAL);
            }

            NotificationManagerCompat nm = NotificationManagerCompat.from(mActivity);
            nm.cancel(userId.hashCode());
        }
    }

    @Override
    protected void findViews() {
        mRecyclerView = f(R.id.rv_chat_details);

        mEtContent        = f(R.id.et_send_message_chat_detail);
        mIvEmoji          = f(R.id.iv_emoji_as_bt);
        mIvSendMsgAddAtcm = f(R.id.iv_send_msg_add_attachment_as_bt);

        mContainerBottom = f(R.id.container_emoji_attachment);
    }

    @Override
    protected void initUI() {
        RxBus.get().post(Def.Event.PREPARE_FOR_FRAGMENT, new Object());
        updateActionbar();

        initRecyclerView();

        updateSendMsgIcon();
    }

    private void updateSendMsgIcon() {
        if (mShouldShowAsSendMessage) {
            Drawable d = ContextCompat.getDrawable(mActivity, R.drawable.act_send);
            Drawable nd = d.mutate();
            nd.setColorFilter(App.getMe().getColor(), PorterDuff.Mode.SRC_ATOP);
            mIvSendMsgAddAtcm.setImageDrawable(nd);
        } else {
            mIvSendMsgAddAtcm.setImageResource(R.drawable.act_add_attachment);
        }
    }

    private void updateActionbar() {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mOther.getName());
        }
    }

    private void initRecyclerView() {
        mAdapter = new ChatDetailsAdapter(mActivity, mChat);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        /**
         * setAdapter后不会立刻渲染、布局View，此时滑动到底部是很难生效的，因此采取下列的做法:
         *
         * 1. 为{@link mRecyclerView}增加一个{@link ViewTreeObserver}，当发现RecyclerView已经布局完成，
         * 就滑动到底部，但此时如果在加载图片，当图片加载完成、布局又增大时，不会自动滑动到底部
         *
         * 2. 为解决1中的问题，在{@link ChatDetailsAdapter#updateCardUiForImage(ChatDetailsAdapter.ChatDetailHolder, ChatDetail)}
         * 中，如果调用了显示图片的逻辑，则添加一个监听器，当图片加载完成，使用RxBus发送一个tag为
         * {@link com.room517.chitchat.Def.Event.CHAT_DETAILS_SCROLL_BOTTOM}的事件，在本Fragment
         * 监听，并调用滑动方法，这样就可以正常地滑动到底部了。注意：这个滑动方法必须是post出的，因为RecyclerView
         * 里还在显示图片。此外，还要加入一个标志位{@link mScrollByUser}，首次滑动完成后设之为{@code true}，以
         * 防止每次加载新图片都自动滑动到底部
         *
         */
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver observer = mRecyclerView.getViewTreeObserver();
                        scrollRecyclerViewToBottom();
                        if (!observer.isAlive()) {
                            return;
                        }
                        if (mScrollByUser) {
                            if (DeviceUtil.hasJellyBeanApi()) {
                                observer.removeOnGlobalLayoutListener(this);
                            } else {
                                observer.removeGlobalOnLayoutListener(this);
                            }
                        }
                    }
                });
    }

    private void scrollRecyclerViewToBottom() {
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        mRecyclerView.scrollBy(0, Integer.MAX_VALUE);
    }

    @Override
    protected void setupEvents() {
        KeyboardUtil.addKeyboardCallback(mActivity.getWindow(), mKeyboardCallback);

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScrollByUser = true;
                return false;
            }
        });

        setupEditTextEvents();
        setupEmojiEvent();
        setupSendMsgEvent();
    }

    private void setupEditTextEvents() {
        mEtContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showOrHideBottomContainer(false);
                }
                return false;
            }
        });
        mEtContent.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mShouldShowAsSendMessage = !s.toString().isEmpty();
                updateSendMsgIcon();
            }
        });
    }

    private void setupEmojiEvent() {
        mIvEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareForBottomContainer();
            }
        });
    }

    private boolean isBottomContainerShowing() {
        return mContainerBottom.getHeight() > 0;
    }

    private void showOrHideBottomContainer(boolean show) {
        boolean isShowing = isBottomContainerShowing();
        if (isShowing ^ show) {
            final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)
                    mContainerBottom.getLayoutParams();

            int from = rlp.height;
            int to   = show ? DisplayUtil.dp2px(200) : 0;

            ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to).setDuration(60);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rlp.height = (int) animation.getAnimatedValue();
                    mContainerBottom.requestLayout();
                }
            });
            valueAnimator.start();
        }
    }

    private void prepareForBottomContainer() {
        mActivity.setShouldHandleBackMyself(false);
        KeyboardUtil.hideKeyboard(mActivity.getCurrentFocus());
        showOrHideBottomContainer(true);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollRecyclerViewToBottom();
            }
        }, 100);
    }

    private void setupSendMsgEvent() {
        mIvSendMsgAddAtcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShouldShowAsSendMessage) {
                    onSendMsgClicked();
                } else {
                    onAddAtcmClicked();
                }
            }
        });
    }

    private void onSendMsgClicked() {
        String content = mEtContent.getText().toString();
        if (content.isEmpty()) {
            return;
        }
        handleSendNewMessage(ChatDetail.TYPE_TEXT, content);
    }

    private void handleSendNewMessage(@ChatDetail.Type int type, String content) {
        String fromId = App.getMe().getId();
        String id     = ChatDetail.newChatDetailId(fromId);
        String toId   = mOther.getId();
        int    state  = ChatDetail.STATE_SENDING;
        long   time   = System.currentTimeMillis();

        ChatDetail chatDetail = new ChatDetail(
                id, fromId, toId, type, state, content, time);
        mChat.getChatDetailsToDisplay().add(chatDetail);
        updateUiForNewChatDetail();
        mEtContent.setText("");

        insertChatDetail(chatDetail);
        sendMessage(chatDetail);

        RxBus.get().post(Def.Event.ON_SEND_MESSAGE, chatDetail);
    }

    private void insertChatDetail(ChatDetail chatDetail) {
        String toId = chatDetail.getToId();
        UserDao userDao = UserDao.getInstance();
        if (userDao.getUserById(toId) == null) {
            userDao.insert(mOther);
        }

        ChatDao chatDao = ChatDao.getInstance();
        if (chatDao.getChat(toId, false) == null) {
            chatDao.insertChat(mChat);
        }
        chatDao.insertChatDetail(chatDetail);
    }

    private void onAddAtcmClicked() {
        prepareForBottomContainer();
        getFragmentManager().beginTransaction()
                .replace(R.id.container_emoji_attachment, new AddAttachmentFragment())
                .commit();
    }

    @Subscribe(tags = {@Tag(Def.Event.SEND_MESSAGE)})
    public void sendMessage(final ChatDetail chatDetail) {
        @ChatDetail.Type int type = chatDetail.getType();
        if (type == ChatDetail.TYPE_TEXT) {
            RongHelper.sendTextMessage(chatDetail, getSendMessageCallback(chatDetail));
        } else if (type == ChatDetail.TYPE_IMAGE) {
            RongHelper.sendImageMessage(chatDetail, getSendImageMessageCallback(chatDetail));
        } else if (type == ChatDetail.TYPE_AUDIO) {
            RongHelper.sendVoiceMessage(chatDetail, getSendMessageCallback(chatDetail));
        }
    }

    private RongIMClient.SendMessageCallback getSendMessageCallback(final ChatDetail chatDetail) {
        return new RongIMClient.SendMessageCallback() {
            @Override
            public void onSuccess(Integer integer) {
                updateChatDetailState(chatDetail, ChatDetail.STATE_NORMAL);
            }

            @Override
            public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                Logger.e(errorCode.getMessage());
                updateChatDetailState(chatDetail, ChatDetail.STATE_SEND_FAILED);
            }
        };
    }

    private RongIMClient.SendImageMessageCallback getSendImageMessageCallback(final ChatDetail chatDetail) {
        return new RongIMClient.SendImageMessageCallback() {
            @Override
            public void onAttached(Message message) { }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                Logger.e(errorCode.getMessage());
                updateChatDetailState(chatDetail, ChatDetail.STATE_SEND_FAILED);
            }

            @Override
            public void onSuccess(Message message) {
                updateChatDetailState(chatDetail, ChatDetail.STATE_NORMAL);
            }

            @Override
            public void onProgress(Message message, int i) { }
        };
    }

    private void updateChatDetailState(ChatDetail chatDetail, @ChatDetail.State int state) {
        chatDetail.setState(state);
        chatDetail.setTime(System.currentTimeMillis());
        mAdapter.notifyStateChanged(chatDetail.getId());
        ChatDao.getInstance().updateChatDetailState(chatDetail.getId(), state);
    }

    private void removeCallbacks() {
        KeyboardUtil.removeKeyboardCallback(mActivity.getWindow(), mKeyboardCallback);
    }

    private void updateUiForNewChatDetail() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            return;
        }
        mAdapter.notifyItemInserted(count - 1);
        scrollRecyclerViewToBottom();
    }

    private View.OnClickListener getCopyListener(
            final SimpleListDialog sld, final ChatDetail chatDetail) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager)
                        mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, chatDetail.getContent());
                clipboardManager.setPrimaryClip(clipData);
                mActivity.showShortToast(R.string.success_copy_to_clipboard);
                sld.dismiss();
            }
        };
    }

    private View.OnClickListener getDeleteListener(
            final SimpleListDialog sld, final ChatDetail chatDetail) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sld.dismiss();
                int index = mChat.indexOfChatDetail(chatDetail.getId());
                if (index == -1) { // interesting
                    Logger.e("Try to delete a chat detail with index=" + index);
                    return;
                }
                deleteChatDetailLocally(chatDetail);
            }
        };
    }

    private void deleteChatDetailLocally(ChatDetail chatDetail) {
        ChatDao.getInstance().deleteChatDetail(chatDetail.getId());

        RxBus.get().post(Def.Event.ON_DELETE_MESSAGE, chatDetail);
    }

    private View.OnClickListener getWithdrawListener(
            final SimpleListDialog sld, final ChatDetail chatDetail) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sld.dismiss();
                int index = mChat.indexOfChatDetail(chatDetail.getId());
                if (index == -1) { // interesting
                    Logger.e("Try to withdraw a chat detail with index=" + index);
                    return;
                }
                tryToWithdrawChatDetail(chatDetail);
            }
        };
    }

    @Subscribe(tags = { @Tag(Def.Event.WITHDRAW_MESSAGE) })
    public void tryToWithdrawChatDetail(ChatDetail chatDetail) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean canWithdraw = sp.getBoolean(
                Def.Key.PrefSettings.CAN_WITHDRAW, true);
        if (canWithdraw) {
            withdrawChatDetail(chatDetail);
        } else {
            AlertDialog ad = new AlertDialog.Builder(App.getMe().getColor())
                    .title(getString(R.string.alert_cannot_withdraw_title))
                    .content(getString(R.string.alert_cannot_withdraw_content))
                    .confirmText(getString(R.string.act_confirm))
                    .build();
            ad.show(mActivity.getFragmentManager(), AlertDialog.class.getName());
        }
    }

    // TODO: 2016/7/4 检查是否真的是自己的对话
    /**
     * 步骤如下：
     * 1. 发送一个特殊的消息A，type为{@link ChatDetail#TYPE_CMD_WITHDRAW}，内容则是待删除的消息的id
     * 2. 对方收到该消息A，找到待撤回的消息并删除，并发送结果B给消息撤回方
     * 3. 撤回方接收到结果B，如果撤回成功，删除相应消息记录，否则提醒用户撤回失败
     */
    private void withdrawChatDetail(ChatDetail chatDetail) {
        chatDetail.setState(ChatDetail.STATE_WITHDRAWING);
        mAdapter.notifyStateChanged(chatDetail.getId());
        ChatDao.getInstance().updateChatDetailState(
                chatDetail.getId(), ChatDetail.STATE_WITHDRAWING);

        String fromId  = App.getMe().getId();
        String id      = ChatDetail.newChatDetailId(fromId);
        String toId    = mOther.getId();
        int type       = ChatDetail.TYPE_CMD_WITHDRAW;
        int state      = ChatDetail.STATE_SENDING;
        // 内容为待撤回消息的id
        String content = chatDetail.getId();
        long time      = System.currentTimeMillis();

        ChatDetail withdraw = new ChatDetail(id, fromId, toId, type, state, content, time);
        ChatDao.getInstance().insertChatDetail(withdraw);
        sendWithdrawMessage(withdraw);
    }

    private void sendWithdrawMessage(final ChatDetail withdraw) {
        RongHelper.sendCmdMessage(withdraw, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                ChatDao chatDao = ChatDao.getInstance();
                chatDao.updateChatDetailState(
                        withdraw.getId(), ChatDetail.STATE_SEND_FAILED);
                chatDao.updateChatDetailState(
                        withdraw.getContent(), ChatDetail.STATE_WITHDRAW_FAILED);
                mAdapter.notifyStateChanged(withdraw.getContent());
            }

            @Override
            public void onSuccess(Integer integer) {
                /*
                    这个时候还不算成功撤回，只是撤回的“申请”已经成功发送了，但是对方不一定收到，也
                    不一定准许该申请
                 */
                ChatDao chatDao = ChatDao.getInstance();
                chatDao.updateChatDetailState(
                        withdraw.getId(), ChatDetail.STATE_NORMAL);
            }
        });
    }

    private KeyboardUtil.KeyboardCallback mKeyboardCallback = new KeyboardUtil.KeyboardCallback() {

        @Override
        public void onKeyboardShow(int keyboardHeight) {
            scrollRecyclerViewToBottom();
        }

        @Override
        public void onKeyboardHide() {
            scrollRecyclerViewToBottom();
        }
    };





    // -------------------------------- Event Subscribers -------------------------------- //


    @Subscribe(tags = { @Tag(Def.Event.ON_BACK_PRESSED_MAIN) })
    public void onBackPressed(Object eventIgnored) {
        if (isBottomContainerShowing()) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment arf = fm.findFragmentByTag(AudioRecordFragment.class.getName());
            if (arf != null) {
                ft.remove(arf);
            }

            ft.commit();

            showOrHideBottomContainer(false);
            mActivity.setShouldHandleBackMyself(true);
        }
    }

    @Subscribe(tags = {@Tag(Def.Event.CHECK_USER_DETAIL)})
    public void checkUserDetail(View view) {
        Intent intent = new Intent(mActivity, UserActivity.class);
        intent.putExtra(Def.Key.USER, mOther);
        ActivityOptionsCompat transition = ActivityOptionsCompat.makeScaleUpAnimation(
                view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
        ActivityCompat.startActivity(mActivity, intent, transition.toBundle());
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_RECEIVE_MESSAGE)})
    public void onReceiveMessage(ChatDetail chatDetail) {
        if (!chatDetail.getFromId().equals(mOther.getId())) {
            return;
        }
        mChat.getChatDetailsToDisplay().add(chatDetail);
        updateUiForNewChatDetail();
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_IMAGE_CHAT_DETAIL_CLICKED)})
    public void onChatDetailClicked(Def.Event.CheckImage checkImage) {
        Intent intent = new Intent(mActivity, ImageViewerActivity.class);
        String[] uris = new String[] { Uri.decode(checkImage.uri) };
        intent.putExtra("urls", uris);

        View view = checkImage.view;
        ActivityOptionsCompat animation =
                ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0
                        , view.getWidth(), view.getHeight());
        ActivityCompat.startActivity(mActivity, intent, animation.toBundle());
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED)})
    public void onChatDetailLongClicked(final ChatDetail chatDetail) {
        final SimpleListDialog sld = new SimpleListDialog();

        List<String> items = new ArrayList<>();
        List<View.OnClickListener> onItemClickListeners = new ArrayList<>();

        int state = chatDetail.getState();
        if (state == ChatDetail.STATE_SEND_FAILED) {
            items.add(getString(R.string.send_again));
            onItemClickListeners.add(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatDetail.setState(ChatDetail.STATE_SENDING);
                    mAdapter.notifyStateChanged(chatDetail.getId());
                    sendMessage(chatDetail);
                    sld.dismiss();
                }
            });
        } else if (state == ChatDetail.STATE_WITHDRAW_FAILED) {
            items.add(getString(R.string.withdraw_again));
            onItemClickListeners.add(getWithdrawListener(sld, chatDetail));
        }

        items.add(getString(R.string.act_copy));
        onItemClickListeners.add(getCopyListener(sld, chatDetail));

        items.add(getString(R.string.act_delete));
        onItemClickListeners.add(getDeleteListener(sld, chatDetail));

        if (chatDetail.getFromId().equals(App.getMe().getId())) {
            items.add(getString(R.string.act_withdraw));
            onItemClickListeners.add(getWithdrawListener(sld, chatDetail));
        }

        sld.setItems(items);
        sld.setOnItemClickListeners(onItemClickListeners);

        sld.show(mActivity.getFragmentManager(), SimpleListDialog.class.getName());
    }

    @Subscribe(tags = { @Tag(Def.Event.IMAGE_PICKED) })
    public void onImagePicked(String pathName) {
        handleSendNewMessage(ChatDetail.TYPE_IMAGE,
                Uri.fromFile(new File(pathName)).toString());
    }

    @Subscribe(tags = { @Tag(Def.Event.RECORD_AUDIO) })
    public void recordAudio(Object eventIgnored) {
        int color = App.getMe().getColor();
        getFragmentManager().beginTransaction()
                .replace(R.id.container_emoji_attachment,
                        AudioRecordFragment.newInstance(color),
                        AudioRecordFragment.class.getName())
                .commit();
    }

    @Subscribe(tags = { @Tag(Def.Event.AUDIO_RECORDED) })
    public void onAudioRecorded(AudioInfo audioInfo) {
        handleSendNewMessage(ChatDetail.TYPE_AUDIO, audioInfo.toJson());
    }

    @Subscribe(tags = { @Tag(Def.Event.CHAT_DETAILS_SCROLL_BOTTOM) })
    public void scrollRecyclerViewToBottom(Object eventIgnored) {
        if (mScrollByUser) {
            return;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                scrollRecyclerViewToBottom();
            }
        });
    }
}
