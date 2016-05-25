package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.User;

/**
 * Created by ywwynm on 2016/5/25.
 * 显示聊天详情的fragment
 */
public class ChatDetailFragment extends BaseFragment {

    public static ChatDetailFragment newInstance(Bundle args) {
        ChatDetailFragment fragment = new ChatDetailFragment();
        fragment.setArguments(args);
        return fragment;
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
    public void onDestroyView() {
        super.onDestroyView();
        RxBus.get().post(Def.Event.BACK_FROM_FRAGMENT, new Object());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chat_detail;
    }

    @Override
    protected void initMember() {
        Bundle args = getArguments();
        User user = args.getParcelable(Def.Key.USER);
        if (user != null) {
            Logger.json(user.toString());
        }
    }

    @Override
    protected void findViews() {

    }

    @Override
    protected void initUI() {

    }

    @Override
    protected void setupEvents() {

    }
}
