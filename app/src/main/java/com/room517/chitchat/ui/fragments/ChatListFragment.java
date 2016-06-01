package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.ui.adapters.ChatListAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ywwynm on 2016/5/24.
 * 对话列表Fragment
 */
public class ChatListFragment extends BaseFragment {

    public static ChatListFragment newInstance(Bundle args) {
        ChatListFragment chatListFragment = new ChatListFragment();
        chatListFragment.setArguments(args);
        return chatListFragment;
    }

    private ChatDao mChatDao;

    private LinearLayout mLlEmpty;
    private ScrollView mScrollView;

    private TextView        mTvChatsSticky;
    private CardView        mCvChatsSticky;
    private RecyclerView    mRvChatsSticky;
    private ChatListAdapter mAdapterSticky;

    private TextView        mTvChatsNormal;
    private CardView        mCvChatsNormal;
    private RecyclerView    mRvChatsNormal;
    private ChatListAdapter mAdapterNormal;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chat_list;
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
        RxBus.get().unregister(this);
    }

    @Override
    protected void initMember() {
        mChatDao = ChatDao.getInstance();
    }

    @Override
    protected void findViews() {
        mLlEmpty    = f(R.id.ll_empty_state_chat_list);
        mScrollView = f(R.id.sv_chat_list);

        mTvChatsSticky = f(R.id.tv_chats_sticky);
        mCvChatsSticky = f(R.id.cv_chats_sticky);
        mRvChatsSticky = f(R.id.rv_chats_sticky);
        mTvChatsNormal = f(R.id.tv_chats_normal);
        mCvChatsNormal = f(R.id.cv_chats_normal);
        mRvChatsNormal = f(R.id.rv_chats_normal);
    }

    @Override
    protected void initUI() {
        setVisibilities();

        if (!mChatDao.noChats()) {
            if (!mChatDao.noStickyChats()) {
                initChatListSticky();
            } else {
                initChatListNormal();
            }
        }
    }

    private void setVisibilities() {
        if (mChatDao.noChats()) { // 没有任何聊天
            mLlEmpty.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        } else {
            mLlEmpty.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (!mChatDao.noStickyChats()) { // 有置顶聊天
                mTvChatsNormal.setVisibility(View.GONE);
                mCvChatsNormal.setVisibility(View.GONE);
            } else { // 只有普通聊天
                mTvChatsSticky.setVisibility(View.GONE);
                mCvChatsSticky.setVisibility(View.GONE);
            }
        }
    }

    private void initChatListSticky() {
        mAdapterSticky = new ChatListAdapter(
                getActivity(), sortChats(mChatDao.getChats(Chat.TYPE_STICKY)), Chat.TYPE_STICKY);
        mRvChatsSticky.setAdapter(mAdapterSticky);
        mRvChatsSticky.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initChatListNormal() {
        mAdapterNormal = new ChatListAdapter(
                getActivity(), sortChats(mChatDao.getChats(Chat.TYPE_NORMAL)), Chat.TYPE_NORMAL);
        mRvChatsNormal.setAdapter(mAdapterNormal);
        mRvChatsNormal.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private List<Chat> sortChats(List<Chat> chats) {
        Collections.sort(chats, new Comparator<Chat>() {
            @Override
            public int compare(Chat c1, Chat c2) {
                ChatDetail cd1 = mChatDao.getLastChatDetail(c1.getUserId());
                ChatDetail cd2 = mChatDao.getLastChatDetail(c2.getUserId());
                Long t1 = cd1.getTime();
                Long t2 = cd2.getTime();
                return -t1.compareTo(t2);
            }
        });
        return chats;
    }

    @Override
    protected void setupEvents() {

    }

    @Subscribe(tags = { @Tag(Def.Event.ON_RECEIVE_MESSAGE) })
    public void onMessageReceived(ChatDetail chatDetail) {
        onNewMessage(chatDetail);
    }

    @Subscribe(tags = { @Tag(Def.Event.ON_SEND_MESSAGE) })
    public void onMessageSent(ChatDetail chatDetail) {
        onNewMessage(chatDetail);
    }

    private void onNewMessage(ChatDetail chatDetail) {
        setVisibilities();
        Chat chat = mChatDao.getChat(chatDetail, false);
        if (chat.getType() == Chat.TYPE_STICKY) {
            if (mAdapterSticky == null) {
                initChatListSticky();
                mAdapterSticky.getUnreadCounts().set(0, 1);
                mAdapterSticky.notifyItemChanged(0);
            }
        } else {
            if (mAdapterNormal == null) {
                initChatListNormal();
                mAdapterNormal.getUnreadCounts().set(0, 1);
                mAdapterNormal.notifyItemChanged(0);
            }
        }
    }
}
