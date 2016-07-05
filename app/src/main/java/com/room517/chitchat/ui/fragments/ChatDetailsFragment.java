package com.room517.chitchat.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.activities.UserActivity;
import com.room517.chitchat.ui.adapters.ChatDetailsAdapter;
import com.room517.chitchat.ui.dialogs.AlertDialog;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;
import com.room517.chitchat.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.RongIMClient;

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
    private ImageView mIvSendMsg;

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

    @Subscribe(tags = {@Tag(Def.Event.CHECK_USER_DETAIL)})
    public void checkUserDetail(View view) {
        Intent intent = new Intent(mActivity, UserActivity.class);
        intent.putExtra(Def.Key.USER, mOther);
        ActivityOptionsCompat transition = ActivityOptionsCompat.makeScaleUpAnimation(
                view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
        ActivityCompat.startActivity(mActivity, intent, transition.toBundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        App.setWrChatDetails(this);
        RxBus.get().register(this);

        super.init();
        return mContentView;
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

        mEtContent = f(R.id.et_send_message_chat_detail);
        mIvSendMsg = f(R.id.iv_send_msg_chat_detail_as_bt);
    }

    @Override
    protected void initUI() {
        RxBus.get().post(Def.Event.PREPARE_FOR_FRAGMENT, new Object());
        updateActionbar();

        initRecyclerView();

        Drawable d = ContextCompat.getDrawable(mActivity, R.drawable.act_send);
        Drawable nd = d.mutate();
        nd.setColorFilter(App.getMe().getColor(), PorterDuff.Mode.SRC_ATOP);
        mIvSendMsg.setImageDrawable(nd);
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
        final int size = mChat.getChatDetailsToDisplay().size();
        if (size > 0) {
            mRecyclerView.scrollToPosition(size - 1);
        }
    }

    @Override
    protected void setupEvents() {
        KeyboardUtil.addKeyboardCallback(mActivity.getWindow(), mKeyboardCallback);
        setupSendMessageEvents();
    }

    private void setupSendMessageEvents() {
        mIvSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEtContent.getText().toString();
                if (content.isEmpty()) {
                    return;
                }

                String fromId = App.getMe().getId();
                String id     = ChatDetail.newChatDetailId(fromId);
                String toId   = mOther.getId();
                int    type   = ChatDetail.TYPE_TEXT;
                int    state  = ChatDetail.STATE_SENDING;
                long   time   = System.currentTimeMillis();

                ChatDetail chatDetail = new ChatDetail(
                        id, fromId, toId, type, state, content, time);
                mChat.getChatDetailsToDisplay().add(chatDetail);
                updateUiForNewChatDetail();
                mEtContent.setText("");

                UserDao userDao = UserDao.getInstance();
                if (userDao.getUserById(toId) == null) {
                    userDao.insert(mOther);
                }

                ChatDao chatDao = ChatDao.getInstance();
                if (chatDao.getChat(toId, false) == null) {
                    chatDao.insertChat(mChat);
                }
                chatDao.insertChatDetail(chatDetail);

                sendTextMessage(chatDetail);

                RxBus.get().post(Def.Event.ON_SEND_MESSAGE, chatDetail);
            }
        });
    }

    @Subscribe(tags = {@Tag(Def.Event.SEND_MESSAGE)})
    public void sendTextMessage(final ChatDetail chatDetail) {
        RongHelper.sendTextMessage(chatDetail, new RongIMClient.SendMessageCallback() {
            @Override
            public void onSuccess(Integer integer) {
                updateChatDetailState(chatDetail, ChatDetail.STATE_NORMAL);
            }

            @Override
            public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                Logger.e(errorCode.getMessage());
                updateChatDetailState(chatDetail, ChatDetail.STATE_SEND_FAILED);
            }
        });
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

    @Subscribe(tags = {@Tag(Def.Event.ON_RECEIVE_MESSAGE)})
    public void onReceiveMessage(ChatDetail chatDetail) {
        if (!chatDetail.getFromId().equals(mOther.getId())) {
            return;
        }
        mChat.getChatDetailsToDisplay().add(chatDetail);
        updateUiForNewChatDetail();
    }

    private void updateUiForNewChatDetail() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            return;
        }
        mAdapter.notifyItemInserted(count - 1);
        mRecyclerView.smoothScrollToPosition(count - 1);
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED)})
    public void onChatDetailLongClicked(final ChatDetail chatDetail) {
        final SimpleListDialog sld = new SimpleListDialog();

        List<String> items = new ArrayList<>();
        List<View.OnClickListener> onItemClickListeners = new ArrayList<>();

        if (chatDetail.getState() == ChatDetail.STATE_SEND_FAILED) {
            items.add(getString(R.string.send_again));
            onItemClickListeners.add(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatDetail.setState(ChatDetail.STATE_SENDING);
                    mAdapter.notifyStateChanged(chatDetail.getId());
                    sendTextMessage(chatDetail);
                    sld.dismiss();
                }
            });
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
                deleteChatDetailLocally(chatDetail.getId());
            }
        };
    }

    private void deleteChatDetailLocally(String id) {
        ChatDao.getInstance().deleteChatDetail(id);

        RxBus.get().post(Def.Event.ON_DELETE_MESSAGE, id);
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
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }

        @Override
        public void onKeyboardHide() {
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    };

    // TODO: 2016/7/4 返回时，如果列表为空，说明所有的对话都没有了，那么应该在主界面删除与该用户的对话
}
