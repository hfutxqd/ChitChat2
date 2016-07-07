package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ywwynm on 2016/5/24.
 * 所有Fragment的基类
 */
public abstract class BaseFragment extends Fragment {

    protected View mContentView;

    protected boolean mShouldBackFromFragment = true;

    public void setShouldBackFromFragment(boolean shouldBackFromFragment) {
        mShouldBackFromFragment = shouldBackFromFragment;
    }

    /**
     * 该方法必须被子类在onCreateView()中调用
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(getLayoutRes(), container, false);

        beforeInit();
        init();

        return mContentView;
    }

    protected abstract @LayoutRes int getLayoutRes();

    protected void beforeInit() { }

    protected void initMember() { }

    protected void findViews() { }

    protected void initUI() { }

    protected void setupEvents() { }

    protected void init() {
        initMember();
        findViews();
        initUI();
        setupEvents();
    }

    protected final <T extends View> T f(View view, @IdRes int id) {
        return (T) view.findViewById(id);
    }

    protected final <T extends View> T f(@IdRes int id) {
        return f(mContentView, id);
    }
}
