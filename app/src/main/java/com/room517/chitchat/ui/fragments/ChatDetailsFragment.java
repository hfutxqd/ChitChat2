package com.room517.chitchat.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.activities.UserActivity;
import com.room517.chitchat.ui.adapters.ChatDetailsAdapter;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;
import com.room517.chitchat.utils.KeyboardUtil;

import java.util.ArrayList;
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

    private EditText  mEtContent;
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

    @Subscribe(tags = { @Tag(Def.Event.CHECK_USER_DETAIL) })
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
        RxBus.get().unregister(this);
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
                String content = mEtContent.getText().toString();
                if (content.isEmpty()) {
                    return;
                }

                long id = ChatDao.getInstance().getNewChatDetailId();

                String fromId = App.getMe().getId();
                String toId   = mOther.getId();
                int    state  = ChatDetail.STATE_SENDING;
                long   time   = System.currentTimeMillis();

                ChatDetail chatDetail = new ChatDetail(id, fromId, toId, state, content, time);
                mChat.getChatDetails().add(chatDetail);
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

                if (id == 1) {
                    /*
                        id=1的时候意味着数据库里没有chat数据了，但这可能有两种情况，即本来就确实没有，以及
                        用户删除了所有的chat。在后一种情况下，我们不能用1作为新的id，因为自增还是会从曾经
                        的数字开始。
                     */
                    id = chatDao.insertChatDetail(chatDetail);
                    chatDetail.setId(id);
                } else {
                    chatDao.insertChatDetail(chatDetail);
                }

                sendMessage(chatDetail);

                RxBus.get().post(Def.Event.ON_SEND_MESSAGE, chatDetail);
            }
        });
    }

    @Subscribe(tags = { @Tag(Def.Event.SEND_MESSAGE) })
    public void sendMessage(final ChatDetail chatDetail) {
        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE,
                chatDetail.getToId(),
                TextMessage.obtain(chatDetail.getContent()),
                null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onSuccess(Integer integer) {
                        updateChatDetailState(chatDetail, ChatDetail.STATE_NORMAL);
                    }

                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                        Logger.e(errorCode.getMessage());
                        updateChatDetailState(chatDetail, ChatDetail.STATE_SEND_FAILED);
                    }
                }, null);
    }

    private void updateChatDetailState(ChatDetail chatDetail, @ChatDetail.State int state) {
        chatDetail.setState(state);
        chatDetail.setTime(System.currentTimeMillis());
        mAdapter.notifyStateChanged(chatDetail);
        ChatDao.getInstance().updateChatDetailState(chatDetail.getId(), state);
    }

    private void removeCallbacks() {
        KeyboardUtil.removeKeyboardCallback(mActivity.getWindow(), mKeyboardCallback);
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_RECEIVE_MESSAGE) })
    public void onReceiveMessage(ChatDetail chatDetail) {
        if (!chatDetail.getFromId().equals(mOther.getId())) {
            return;
        }
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

    @Subscribe(tags = { @Tag(Def.Event.ON_CHAT_DETAIL_LONG_CLICKED) })
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
                    mAdapter.notifyStateChanged(chatDetail);
                    sendMessage(chatDetail);
                    sld.dismiss();
                }
            });
        }

        items.add(getString(R.string.act_copy));
        onItemClickListeners.add(getCopyListener(sld, chatDetail));

        items.add(getString(R.string.act_delete));
        onItemClickListeners.add(getDeleteListener(sld, chatDetail));

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
            }
        };
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
