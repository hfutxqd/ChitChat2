package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.NotificationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.adapters.ChatListAdapter;
import com.room517.chitchat.ui.dialogs.AlertDialog;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import retrofit2.Retrofit;

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

    private TextView[] mTvChats;
    private CardView[] mCvChats;
    private RecyclerView[] mRvChats;
    private ChatListAdapter[] mAdapters;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chat_list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        App.setWrChatList(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.setWrChatList(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Bus rxBus = RxBus.get();
        for (ChatListAdapter adapter : mAdapters) {
            if (adapter != null) {
                rxBus.unregister(adapter);
            }
        }
        rxBus.unregister(this);
        App.setWrChatList(null);
    }

    @Subscribe(tags = {@Tag(Def.Event.CLEAR_NOTIFICATIONS)})
    public void clearNotifications(Object event) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(getActivity());
        for (ChatListAdapter adapter : mAdapters) {
            if (adapter != null) {
                for (Chat chat : adapter.getChats()) {
                    String userId = chat.getUserId();
                    manager.cancel(userId.hashCode());
                    NotificationHelper.putUnreadCount(userId, 0);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void beforeInit() {
        RxBus.get().register(this);
        App.setWrChatList(this);
    }

    @Override
    protected void initMember() {
        mChatDao = ChatDao.getInstance();

        mTvChats = new TextView[2];
        mCvChats = new CardView[2];
        mRvChats = new RecyclerView[2];
        mAdapters = new ChatListAdapter[2];
    }

    @Override
    protected void findViews() {
        mLlEmpty = f(R.id.ll_empty_state_chat_list);
        mScrollView = f(R.id.sv_chat_list);

        mTvChats[0] = f(R.id.tv_chats_normal);
        mCvChats[0] = f(R.id.cv_chats_normal);
        mRvChats[0] = f(R.id.rv_chats_normal);
        mTvChats[1] = f(R.id.tv_chats_sticky);
        mCvChats[1] = f(R.id.cv_chats_sticky);
        mRvChats[1] = f(R.id.rv_chats_sticky);
    }

    @Override
    protected void initUI() {
        setVisibilities();

        if (!mChatDao.noChats()) {
            if (!mChatDao.noNormalChats()) {
                initChatList(Chat.TYPE_NORMAL);
            }
            if (!mChatDao.noStickyChats()) {
                initChatList(Chat.TYPE_STICKY);
            }
            updateUsers();
        }
    }

    private void setVisibilities() {
        if (mChatDao.noChats()) { // 没有任何聊天
            mLlEmpty.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        } else {
            mLlEmpty.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);

            if (mChatDao.noNormalChats()) { // 没有普通聊天
                mTvChats[0].setVisibility(View.GONE);
                mCvChats[0].setVisibility(View.GONE);
            } else {
                mTvChats[0].setVisibility(View.VISIBLE);
                mCvChats[0].setVisibility(View.VISIBLE);
            }

            if (mChatDao.noStickyChats()) { // 没有置顶聊天
                mTvChats[1].setVisibility(View.GONE);
                mCvChats[1].setVisibility(View.GONE);
            } else {
                mTvChats[1].setVisibility(View.VISIBLE);
                mCvChats[1].setVisibility(View.VISIBLE);
            }
        }
    }

    private void initChatList(@Chat.Type int type) {
        mAdapters[type] = new ChatListAdapter(
                getActivity(), sortChats(mChatDao.getChats(type)), type);
        mRvChats[type].setAdapter(mAdapters[type]);
        mRvChats[type].setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private List<Chat> sortChats(List<Chat> chats) {
        Collections.sort(chats, new Comparator<Chat>() {
            @Override
            public int compare(Chat c1, Chat c2) {
                ChatDetail cd1 = mChatDao.getLastChatDetailToDisplay(c1.getUserId());
                ChatDetail cd2 = mChatDao.getLastChatDetailToDisplay(c2.getUserId());
                if (cd1 == null || cd2 == null) {
                    return 0;
                }
                Long t1 = cd1.getTime();
                Long t2 = cd2.getTime();
                return -t1.compareTo(t2);
            }
        });
        return chats;
    }

    private void updateUsers() {
        for (ChatListAdapter adapter : mAdapters) {
            if (adapter != null) {
                List<Chat> chats = adapter.getChats();
                final int size = chats.size();
                String[] userIds = new String[size];
                for (int i = 0; i < size; i++) {
                    userIds[i] = chats.get(i).getUserId();
                }
                updateUsers(userIds, adapter);
            }
        }
    }

    private void updateUsers(String[] userIds, final ChatListAdapter adapter) {
        Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
        UserService service = retrofit.create(UserService.class);
        RxHelper.ioMain(service.getUsersByIds(userIds),
                new SimpleObserver<User[]>() {
                    @Override
                    public void onNext(User[] users) {
                        UserDao dao = UserDao.getInstance();
                        for (User user : users) {
                            if (user == null) continue;
                            dao.update(user);

                            List<User> curUsers = adapter.getUsers();
                            int pos = adapter.getInfoPosition(user.getId());
                            if (pos != -1) {
                                curUsers.set(pos, user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void setupEvents() {
        MainActivity activity = (MainActivity) getActivity();
        activity.getFab().attachToScrollView(mScrollView);
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_RECEIVE_MESSAGE)})
    public void onMessageReceived(ChatDetail chatDetail) {
        onNewMessage(chatDetail);
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_SEND_MESSAGE)})
    public void onMessageSent(ChatDetail chatDetail) {
        onNewMessage(chatDetail);
    }

    private void onNewMessage(ChatDetail chatDetail) {
        setVisibilities();
        Chat chat = mChatDao.getChat(chatDetail, false);
        int type = chat.getType();
        if (mAdapters[type] == null) {
            initChatList(type);
            mAdapters[type].getUnreadCounts().set(0, 1);
            mAdapters[type].notifyItemChanged(0);
        }
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_CHAT_LIST_LONG_CLICKED)})
    public void onChatListLongClicked(Chat chat) {
        SimpleListDialog sld = new SimpleListDialog();

        List<String> items = new ArrayList<>();
        List<View.OnClickListener> onItemClickListeners = new ArrayList<>();

        if (chat.getType() == Chat.TYPE_NORMAL) {
            items.add(getString(R.string.act_sticky_on_top));
        } else {
            items.add(getString(R.string.act_remove_from_top));
        }
        onItemClickListeners.add(getStickyListener(sld, chat));

        items.add(getString(R.string.act_delete));
        onItemClickListeners.add(getDeleteListener(sld, chat));

        sld.setItems(items);
        sld.setOnItemClickListeners(onItemClickListeners);

        sld.show(getActivity().getFragmentManager(), SimpleListDialog.class.getName());
    }

    private View.OnClickListener getStickyListener(final SimpleListDialog sld, final Chat chat) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = chat.getUserId();
                @Chat.Type int fromType = chat.getType();
                @Chat.Type int toType = 1 - fromType;

                chat.setType(toType);

                HashMap<String, Object> infoMap = mAdapters[fromType].getInfoMap(userId);
                mAdapters[fromType].remove(userId);
                if (mAdapters[fromType].getUnreadCounts().isEmpty()) {
                    mTvChats[fromType].setVisibility(View.GONE);
                    mCvChats[fromType].setVisibility(View.GONE);
                }

                if (mAdapters[toType] == null) {
                    initChatList(toType);
                }
                mTvChats[toType].setVisibility(View.VISIBLE);
                mCvChats[toType].setVisibility(View.VISIBLE);
                mAdapters[toType].add(infoMap, toType == Chat.TYPE_NORMAL);
                mAdapters[toType].notifyDataSetChanged();

                mChatDao.updateChat(userId, toType);

                sld.dismiss();
            }
        };
    }

    private View.OnClickListener getDeleteListener(final SimpleListDialog sld, final Chat chat) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View.OnClickListener confirmListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int type = chat.getType();
                        mAdapters[type].remove(chat.getUserId());
                        mChatDao.deleteChat(chat.getUserId());
                        setVisibilities();
                    }
                };
                AlertDialog ad = new AlertDialog.Builder(Def.Meta.APP_PURPLE)
                        .title(getString(R.string.alert_delete_chat_title))
                        .content(getString(R.string.alert_delete_chat_content))
                        .confirmText(getString(R.string.act_confirm))
                        .confirmListener(confirmListener)
                        .cancelText(getString(R.string.act_cancel))
                        .build();
                sld.dismiss();
                ad.show(getActivity().getFragmentManager(), AlertDialog.class.getName());
            }
        };
    }
}
