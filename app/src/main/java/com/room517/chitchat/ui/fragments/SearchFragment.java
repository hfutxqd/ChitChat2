package com.room517.chitchat.ui.fragments;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.ui.adapters.ChatListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ywwynm on 2016/7/12.
 * 显示搜索结果的Fragment
 */
public class SearchFragment extends BaseFragment {

    private LinearLayout mLlEmpty;

    private NestedScrollView mScrollView;

    private TextView[]     mTextViews;
    private CardView[]     mCardViews;
    private RecyclerView[] mRecyclerViews;

    private RecyclerView.Adapter[] mAdapters;

    private UserDao mUserDao;
    private ChatDao mChatDao;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxBus.get().unregister(this);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_search;
    }

    @Override
    protected void beforeInit() {
        RxBus.get().register(this);
    }

    @Override
    protected void initMember() {
        mTextViews     = new TextView[3];
        mCardViews     = new CardView[3];
        mRecyclerViews = new RecyclerView[3];
        mAdapters      = new RecyclerView.Adapter[3];

        mUserDao = UserDao.getInstance();
        mChatDao = ChatDao.getInstance();
    }

    @Override
    protected void findViews() {
        mLlEmpty = f(R.id.ll_empty_state_search);

        mScrollView = f(R.id.sv_search);

        mTextViews[0] = f(R.id.tv_friend_search);
        mTextViews[1] = f(R.id.tv_chat_details_search);

        mCardViews[0] = f(R.id.cv_friend_search);
        mCardViews[1] = f(R.id.cv_chat_details_search);

        mRecyclerViews[0] = f(R.id.rv_friend_search);
        mRecyclerViews[1] = f(R.id.rv_chat_details_search);
    }

    @Override
    protected void initUI() {
        mRecyclerViews[1].setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Subscribe(tags = { @Tag(Def.Event.SEARCH) })
    public void search(String key) {
        if (key.isEmpty()) {
            return;
        }

        boolean friends     = searchFriends(key);
        boolean chatDetails = searchChatDetails(key);
        boolean explore     = searchExplore(key);

        if (chatDetails || friends || explore) {
            mLlEmpty.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        }

        if (friends) {
            mTextViews[0].setVisibility(View.VISIBLE);
            mCardViews[0].setVisibility(View.VISIBLE);
        } else {
            mTextViews[0].setVisibility(View.GONE);
            mCardViews[0].setVisibility(View.GONE);
        }

        if (chatDetails) {
            mTextViews[1].setVisibility(View.VISIBLE);
            mCardViews[1].setVisibility(View.VISIBLE);
        } else {
            mTextViews[1].setVisibility(View.GONE);
            mCardViews[1].setVisibility(View.GONE);
        }
    }

    private boolean searchFriends(String key) {
        return false;
    }

    private boolean searchChatDetails(String key) {
        List<ChatDetail> chatDetails = mChatDao.searchChatDetails(key);
        if (chatDetails.isEmpty()) {
            return false;
        }

        sortChatDetails(chatDetails);
        List<Chat> chats = getChats(chatDetails);
        if (mAdapters[1] != null) {
            RxBus.get().unregister(mAdapters[1]);
        }
        mAdapters[1] = new ChatListAdapter(getActivity(), chats, ChatListAdapter.TYPE_SEARCH);
        ((ChatListAdapter) mAdapters[1]).setChatDetails(chatDetails);
        mRecyclerViews[1].setAdapter(mAdapters[1]);
        return true;
    }

    private boolean searchExplore(String key) {
        return false;
    }

    private void sortChatDetails(List<ChatDetail> chatDetails) {
        Collections.sort(chatDetails, new Comparator<ChatDetail>() {
            @Override
            public int compare(ChatDetail lhs, ChatDetail rhs) {
                return (int) (rhs.getTime() - lhs.getTime());
            }
        });
    }

    private List<Chat> getChats(List<ChatDetail> chatDetails) {
        List<Chat> chats = new ArrayList<>(chatDetails.size());
        for (ChatDetail chatDetail : chatDetails) {
            chats.add(mChatDao.getChat(chatDetail, false));
        }
        return chats;
    }
}
