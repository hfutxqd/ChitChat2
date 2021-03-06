package com.room517.chitchat.ui.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.provider.MediaStore;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amap.api.location.AMapLocation;
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
import com.room517.chitchat.helpers.AMapLocationHelper;
import com.room517.chitchat.helpers.RongHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.model.AudioInfo;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.simpleinterface.SimpleTextWatcher;
import com.room517.chitchat.ui.adapters.ChatDetailsAdapter;
import com.room517.chitchat.ui.dialogs.AlertDialog;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;
import com.room517.chitchat.ui.fragments.AddAttachmentFragment;
import com.room517.chitchat.ui.fragments.AudioRecordFragment;
import com.room517.chitchat.ui.fragments.EmojiKeyboardFragment;
import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.DisplayUtil;
import com.room517.chitchat.utils.FileUtil;
import com.room517.chitchat.utils.KeyboardUtil;
import com.ywwynm.emoji.EmojiEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import xyz.imxqd.photochooser.constant.Constant;

public class ChatDetailsActivity extends BaseActivity {

    private static List<String> chatDetailActivities = new ArrayList<>();

    private User mOther;
    private Chat mChat;

    private Toolbar mActionbar;

    private RecyclerView mRecyclerView;
    private ChatDetailsAdapter mAdapter;

    private EmojiEditText mEtContent;
    private ImageView mIvEmoji;
    private ImageView mIvSendMsgAddAtcm;

    // 控制“发送及添加附件”按钮，如果为true，则显示发送图标，否则为添加附件图标
    private boolean mShouldShowAsSendMessage = false;

    private boolean mScrollByUser = false;

    private FrameLayout mContainerBottom;

    // 拍摄照片后，照片的路径
    private String mPhotoPathName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        chatDetailActivities.add(toString());

        RxBus.get().register(this);

        super.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.setWrChatDetails(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.setWrChatDetails(null);

        mAdapter.stopPlaying();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        chatDetailActivities.remove(toString());

        Bus rxBus = RxBus.get();
        rxBus.post(Def.Event.CLEAR_UNREAD, mOther);
        if (App.getChatListReference() != null) {
            rxBus.post(Def.Event.CLEAR_NOTIFICATIONS, new Object());
        }

        rxBus.unregister(mAdapter);
        rxBus.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.act_check_user_detail) {
            View view = f(R.id.act_check_user_detail);
            Intent intent = new Intent(this, UserActivity.class);
            intent.putExtra(Def.Key.USER, mOther);
            ActivityOptionsCompat transition = ActivityOptionsCompat.makeScaleUpAnimation(
                    view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
            ActivityCompat.startActivity(this, intent, transition.toBundle());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == Def.Request.TAKE_PHOTO) {
            onImagePicked(mPhotoPathName);
        } else if (requestCode == Def.Request.PICK_IMAGE) {
            final ArrayList<String> images = data.getStringArrayListExtra(Constant.EXTRA_PHOTO_PATHS);
            if (images == null || images.size() != 1) {
                return;
            }
            onImagePicked(images.get(0));
        }
    }

    private void onImagePicked(String pathName) {
        handleSendNewMessage(ChatDetail.TYPE_IMAGE,
                Uri.fromFile(new File(pathName)).toString());
    }

    public Chat getChat() {
        return mChat;
    }

    @Override
    protected void initMember() {
        Intent intent = getIntent();
        mOther = intent.getParcelableExtra(Def.Key.USER);
        if (mOther != null) {
            String userId = mOther.getId();
            mChat = ChatDao.getInstance().getChat(userId, true);
            if (mChat == null) {
                mChat = new Chat(userId, Chat.TYPE_NORMAL);
            }

            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            nm.cancel(userId.hashCode());
        }
    }

    @Override
    protected void findViews() {
        mActionbar = f(R.id.actionbar);

        mRecyclerView = f(R.id.rv_chat_details);

        mEtContent        = f(R.id.et_send_message_chat_detail);
        mIvEmoji          = f(R.id.iv_emoji_as_bt);
        mIvSendMsgAddAtcm = f(R.id.iv_send_msg_add_attachment_as_bt);

        mContainerBottom = f(R.id.container_emoji_attachment);
    }

    @Override
    protected void initUI() {
        setSupportActionBar(mActionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mOther.getName());
        }
        mEtContent.setEmojiSize(DisplayUtil.dp2px(32));
        initRecyclerView();

        updateSendMsgIcon();
    }

    private void initRecyclerView() {
        mAdapter = new ChatDetailsAdapter(this, mChat);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ChatDetail toScroll = getIntent().getParcelableExtra(Def.Key.CHAT_DETAIL_SCROLL);
        if (toScroll != null) {
            int index = mChat.indexOfChatDetail(toScroll.getId());
            if (index != -1) {
                mRecyclerView.scrollToPosition(index);
            }
            return;
        }

        ChatDetail toForward = getIntent().getParcelableExtra(Def.Key.CHAT_DETAIL);
        if (toForward != null) {
            handleSendNewMessage(toForward.getType(), toForward.getContent());
        }

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

    private void updateSendMsgIcon() {
        if (mShouldShowAsSendMessage) {
            Drawable d = ContextCompat.getDrawable(this, R.drawable.act_send);
            Drawable nd = d.mutate();
            nd.setColorFilter(App.getMe().getColor(), PorterDuff.Mode.SRC_ATOP);
            mIvSendMsgAddAtcm.setImageDrawable(nd);
        } else {
            mIvSendMsgAddAtcm.setImageResource(R.drawable.act_add_attachment);
        }
    }

    @Override
    protected void setupEvents() {
        mActionbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        KeyboardUtil.addKeyboardCallback(getWindow(), new KeyboardUtil.KeyboardCallback() {
            @Override
            public void onKeyboardShow(int keyboardHeight) {
                scrollRecyclerViewToBottom();
            }

            @Override
            public void onKeyboardHide() {
                scrollRecyclerViewToBottom();
            }
        });

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
                    scrollRecyclerViewToBottom();
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
                prepareAndShowBottomContainer();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_emoji_attachment, new EmojiKeyboardFragment())
                        .commit();
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

            if (!show) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment arf = fm.findFragmentByTag(AudioRecordFragment.class.getName());
                if (arf != null) {
                    ft.remove(arf);
                }

                ft.commit();
            }
        }
    }

    private void prepareAndShowBottomContainer() {
        KeyboardUtil.hideKeyboard(getCurrentFocus());
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
        prepareAndShowBottomContainer();
        getSupportFragmentManager().beginTransaction()
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
        } else if (type == ChatDetail.TYPE_LOCATION) {
            RongHelper.sendLocationMessage(chatDetail, getSendMessageCallback(chatDetail));
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
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, chatDetail.getContent());
                clipboardManager.setPrimaryClip(clipData);
                showShortToast(R.string.success_copy_to_clipboard);
                sld.dismiss();
            }
        };
    }

    private View.OnClickListener getForwardListener(
            final SimpleListDialog sld, final ChatDetail chatDetail) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sld.dismiss();
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ChatDetailsActivity.this, UserChooserActivity.class);
                        intent.putParcelableArrayListExtra(
                                Def.Key.USERS, UserDao.getInstance().searchUsers(""));
                        intent.putExtra(Def.Key.CHAT_DETAIL, chatDetail);
                        startActivity(intent);
                    }
                }, 200);
            }
        };
    }

    private View.OnClickListener getDeleteListener(
            final SimpleListDialog sld, final ChatDetail chatDetail) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sld.dismiss();
                String id = chatDetail.getId();
                if (id.equals(mAdapter.getPlayingId())) {
                    mAdapter.stopPlaying();
                }

                int index = mChat.indexOfChatDetail(id);
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
                String id = chatDetail.getId();
                if (id.equals(mAdapter.getPlayingId())) {
                    mAdapter.stopPlaying();
                }

                int index = mChat.indexOfChatDetail(id);
                if (index == -1) { // interesting
                    Logger.e("Try to withdraw a chat detail with index=" + index);
                    return;
                }
                tryToWithdrawChatDetail(chatDetail);
            }
        };
    }

    @Subscribe(tags = { @Tag(Def.Event.INPUT_EMOJI) })
    public void onInputEmoji(String str) {
        mEtContent.append(str);
    }

    @Subscribe(tags = { @Tag(Def.Event.WITHDRAW_MESSAGE) })
    public void tryToWithdrawChatDetail(ChatDetail chatDetail) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
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
            ad.show(getFragmentManager(), AlertDialog.class.getName());
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

    @Override
    public void onBackPressed() {
        if (isBottomContainerShowing()) {
            showOrHideBottomContainer(false);
        } else {
            super.onBackPressed();
        }
    }





    // -------------------------------- Event Subscribers -------------------------------- //



    @Subscribe(tags = { @Tag(Def.Event.CHECK_USER_DETAIL) })
    public void checkUserDetail(View view) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(Def.Key.USER, mOther);
        ActivityOptionsCompat transition = ActivityOptionsCompat.makeScaleUpAnimation(
                view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
        ActivityCompat.startActivity(this, intent, transition.toBundle());
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_RECEIVE_MESSAGE) })
    public void onReceiveMessage(ChatDetail chatDetail) {
        if (!chatDetail.getFromId().equals(mOther.getId())) {
            return;
        }

        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        mChat.getChatDetailsToDisplay().add(chatDetail);
        updateUiForNewChatDetail();
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_IMAGE_CHAT_DETAIL_CLICKED) })
    public void onChatDetailClicked(Def.Event.CheckImage checkImage) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        Intent intent = new Intent(this, ImageViewerActivity.class);
        String[] uris = new String[] { Uri.decode(checkImage.uri) };
        intent.putExtra("urls", uris);

        View view = checkImage.view;
        ActivityOptionsCompat animation =
                ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0
                        , view.getWidth(), view.getHeight());
        ActivityCompat.startActivity(this, intent, animation.toBundle());
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED) })
    public void onChatDetailLongClicked(final ChatDetail chatDetail) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

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

        items.add(getString(R.string.act_forward));
        onItemClickListeners.add(getForwardListener(sld, chatDetail));

        if (chatDetail.canCopy()) {
            items.add(getString(R.string.act_copy));
            onItemClickListeners.add(getCopyListener(sld, chatDetail));
        }

        items.add(getString(R.string.act_delete));
        onItemClickListeners.add(getDeleteListener(sld, chatDetail));

        if (chatDetail.getFromId().equals(App.getMe().getId())
                && state == ChatDetail.STATE_NORMAL) {
            items.add(getString(R.string.act_withdraw));
            onItemClickListeners.add(getWithdrawListener(sld, chatDetail));
        }

        sld.setItems(items);
        sld.setOnItemClickListeners(onItemClickListeners);

        sld.showAllowingStateLoss(getFragmentManager(), SimpleListDialog.class.getName());
    }

    @Subscribe(tags = { @Tag(Def.Event.TAKE_PHOTO) })
    public void takePhotoForNewMessage(Object eventIgnored) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            showLongToast(R.string.error_activity_not_found);
        } else {
            doWithPermissionChecked(new SimplePermissionCallback() {
                @Override
                public void onGranted() {
                    String fileNameWithPostfix = FileUtil.newSimpleFileName() + ".jpg";
                    File file = FileUtil.createFile(
                            Def.Meta.APP_DIR + "/photo", fileNameWithPostfix);
                    if (file == null) {
                        return;
                    }
                    mPhotoPathName = file.getAbsolutePath();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, Def.Request.TAKE_PHOTO);
                }
            }, Def.Request.TAKE_PHOTO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Subscribe(tags = { @Tag(Def.Event.PICK_IMAGE) })
    public void pickImageForNewMessage(Object eventIgnored) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        doWithPermissionChecked(new SimplePermissionCallback() {
            @Override
            public void onGranted() {
                Intent intent = new Intent("com.room517.chitchat.action.CHOSE_PHOTOS");
                intent.putExtra(Constant.EXTRA_PHOTO_LIMIT, 1);
                startActivityForResult(intent, Def.Request.PICK_IMAGE);
            }
        }, Def.Request.PICK_IMAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Subscribe(tags = { @Tag(Def.Event.RECORD_AUDIO) })
    public void recordAudio(Object eventIgnored) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        doWithPermissionChecked(
                new SimplePermissionCallback() {
                    @Override
                    public void onGranted() {
                        int color = App.getMe().getColor();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container_emoji_attachment,
                                        AudioRecordFragment.newInstance(color),
                                        AudioRecordFragment.class.getName())
                                .commitAllowingStateLoss();
                    }
                },
                Def.Request.RECORD_AUDIO,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Subscribe(tags = { @Tag(Def.Event.AUDIO_RECORDED) })
    public void onAudioRecorded(AudioInfo audioInfo) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }
        if(audioInfo.getDuration() < 1000) {
            showShortToast(R.string.audio_too_short);
            return;
        }

        handleSendNewMessage(ChatDetail.TYPE_AUDIO, audioInfo.toJson());
    }

    @Subscribe(tags = { @Tag(Def.Event.LOCATE_ME) })
    public void locateMe(Object eventIgnored) {
        if (!chatDetailActivities.get(chatDetailActivities.size() - 1).equals(toString())) {
            return;
        }

        Observable.create(new Observable.OnSubscribe<AMapLocation>() {
            @Override
            public void call(Subscriber<? super AMapLocation> subscriber) {
                subscriber.onNext(App.getLocationHelper().getLocationSync());
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<AMapLocation>() {
                    @Override
                    public void onNext(AMapLocation location) {
                        if (location == null) {
                            location = App.getLocationHelper().getLastKnownLocation();
                            if (location == null) {
                                showLongToast(R.string.error_cannot_get_location);
                                return;
                            }
                        }
                        final AMapLocation fLocation = location;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleSendNewMessage(ChatDetail.TYPE_LOCATION,
                                        AMapLocationHelper.getLocationString(fLocation));
                            }
                        });
                    }
                });
    }

}
