package com.room517.chitchat.ui.fragments;

import android.os.Bundle;

import com.room517.chitchat.R;

/**
 * Created by ywwynm on 2016/5/24.
 * 朋友圈列表Fragment
 */
public class ExploreListFragment extends BaseFragment {

    public static ExploreListFragment newInstance(Bundle args) {
        ExploreListFragment exploreListFragment = new ExploreListFragment();
        exploreListFragment.setArguments(args);
        return exploreListFragment;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_explore_list;
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
