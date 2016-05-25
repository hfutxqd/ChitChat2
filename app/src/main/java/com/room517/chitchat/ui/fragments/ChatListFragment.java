package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;

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

    private TextView     mTvChatsTop;
    private CardView     mCvChatsTop;
    private RecyclerView mRvChatsTop;
    private TextView     mTvChatsNormal;
    private CardView     mCvChatsNormal;
    private RecyclerView mRvChatsNormal;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chat_list;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        super.init();
        return mContentView;
    }

    @Override
    protected void initMember() {
        mChatDao = ChatDao.getInstance();
    }

    @Override
    protected void findViews() {
        mLlEmpty    = f(R.id.ll_empty_state_chat_list);
        mScrollView = f(R.id.sv_chat_list);

        mTvChatsTop    = f(R.id.tv_chats_top);
        mCvChatsTop    = f(R.id.cv_chats_top);
        mRvChatsTop    = f(R.id.rv_chats_top);
        mTvChatsNormal = f(R.id.tv_chats_normal);
        mTvChatsNormal = f(R.id.tv_chats_normal);
        mTvChatsNormal = f(R.id.tv_chats_normal);
    }

    @Override
    protected void initUI() {
        setState(mChatDao.noChat());
    }

    private void setState(boolean empty) {
        if (empty) {
            mLlEmpty.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        } else {
            mLlEmpty.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void setupEvents() {

    }
}
