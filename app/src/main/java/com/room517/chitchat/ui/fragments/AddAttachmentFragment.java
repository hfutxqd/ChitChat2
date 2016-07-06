package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ywwynm on 2016/7/6.
 * 一个用来显示要发送哪种媒体消息的Fragment
 */
public class AddAttachmentFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        super.init();
        return mContentView;
    }

    @Override
    protected int getLayoutRes() {
        return 0;
    }

    @Override
    protected void initMember() {

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
