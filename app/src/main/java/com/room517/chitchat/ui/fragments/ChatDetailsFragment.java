package com.room517.chitchat.ui.fragments;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.adapters.ChatDetailsAdapter;
import com.room517.chitchat.utils.KeyboardUtil;

import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;

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

    private RecyclerView       mRecyclerView;
    private ChatDetailsAdapter mAdapter;

    private ImageView mIvSendMsg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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
        rxBus.post(Def.Event.BACK_FROM_FRAGMENT, new Object());
        rxBus.unregister(this);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chat_details;
    }

    @Override
    protected void initMember() {
        mActivity = (MainActivity) getActivity();

        Bundle args = getArguments();
        mOther = args.getParcelable(Def.Key.USER);
        if (mOther != null) {
            Logger.json(mOther.toString());
            String userId = mOther.getId();
            mChat = ChatDao.getInstance().getChat(userId, true);
            if (mChat == null) {
                Logger.i("chat is null");
                mChat = new Chat(userId, Chat.TYPE_NORMAL);
            }
        }
    }

    @Override
    protected void findViews() {
        mRecyclerView = f(R.id.rv_chat_details);

        mIvSendMsg = f(R.id.iv_send_msg_chat_detail_as_bt);
    }

    // TODO: 2016/5/26 delete this method when release.
    private void testChatUi() {
        String meId    = App.getMe().getId();
        String otherId = mOther.getId();

        List<ChatDetail> chatDetails = mChat.getChatDetails();
        chatDetails.add(new ChatDetail(0, meId, otherId, 0, "hello", 0));
        chatDetails.add(new ChatDetail(1, otherId, meId, 0, "hello", 1));
        chatDetails.add(new ChatDetail(2, meId, otherId, 0, "This app is interesting.", 2));
        chatDetails.add(new ChatDetail(3, meId, otherId, 0, "Do you think so?", 3));
        chatDetails.add(new ChatDetail(4, otherId, meId, 0,
                "Yes, it's very different from other im apps. Simple but beautiful. " +
                        "And we can meet other guys here.", 4));
        chatDetails.add(new ChatDetail(5, meId, otherId, 0,
                "Emmm, but it seems that it is under development.", 5));
        chatDetails.add(new ChatDetail(6, otherId, meId, 0, "Yes.", 6));
        chatDetails.add(new ChatDetail(7, otherId, meId, 0, "Maybe we should donate the developer team", 7));
        chatDetails.add(new ChatDetail(8, meId, otherId, 0,
                "haha, I agree with you~But do you know their email or facebook?", 8));
        chatDetails.add(new ChatDetail(9, otherId, meId, 0, "Yes, let's talk with them.", 9));
    }

    @Override
    protected void initUI() {
        RxBus.get().post(Def.Event.PREPARE_FOR_FRAGMENT, new Object());
        updateActionbar();
        initRecyclerView();

        Drawable d  = ContextCompat.getDrawable(mActivity, R.drawable.act_send);
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
        //testChatUi();
        mAdapter = new ChatDetailsAdapter(mActivity, mChat);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        final int size = mChat.getChatDetails().size();
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
                EditText et = f(R.id.et_send_message_chat_detail);
                String content = et.getText().toString();
                if (content.isEmpty()) {
                    return;
                }

                long   id     = ChatDao.getInstance().getNewChatDetailId();
                String fromId = App.getMe().getId();
                String toId   = mOther.getId();
                int    state  = ChatDetail.STATE_SENDING;
                long   time   = System.currentTimeMillis();

                ChatDetail chatDetail = new ChatDetail(id, fromId, toId, state, content, time);
                mChat.getChatDetails().add(chatDetail);
                updateUiForNewChatDetail();
                et.setText("");

                UserDao userDao = UserDao.getInstance();
                if (userDao.getUserById(toId) == null) {
                    userDao.insert(mOther);
                }

                ChatDao chatDao = ChatDao.getInstance();
                if (chatDao.getChat(toId, false) == null) {
                    chatDao.insertChat(mChat);
                }
                chatDao.insertChatDetail(chatDetail);

                sendMessage(chatDetail);

                RxBus.get().post(Def.Event.ON_SEND_MESSAGE, chatDetail);
            }
        });
    }

    private void sendMessage(ChatDetail chatDetail) {
        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE,
                chatDetail.getToId(),
                TextMessage.obtain(chatDetail.getContent()),
                null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onSuccess(Integer integer) {
                        Logger.i("send success! " + integer);
                    }

                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                        Logger.e(errorCode.getMessage());
                    }
                }, null);
    }

    private void removeCallbacks() {
        KeyboardUtil.removeKeyboardCallback(mActivity.getWindow(), mKeyboardCallback);
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_RECEIVE_MESSAGE) })
    public void onReceiveMessage(ChatDetail chatDetail) {
        mChat.getChatDetails().add(chatDetail);
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
}
