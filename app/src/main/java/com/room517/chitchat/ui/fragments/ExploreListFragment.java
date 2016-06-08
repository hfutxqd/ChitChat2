package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.room517.chitchat.R;
import com.room517.chitchat.ui.adapters.ExploreListAdapter;

/**
 * Created by ywwynm on 2016/5/24.
 * 朋友圈列表Fragment
 */
public class ExploreListFragment extends BaseFragment {

    private RecyclerView mList;
    private ExploreListAdapter mAdapter;

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
        mAdapter = new ExploreListAdapter();
    }

    @Override
    protected void findViews() {
        mList = f(R.id.explore_list);
    }

    @Override
    protected void initUI() {
        mList.setAdapter(mAdapter);
    }

    @Override
    protected void setupEvents() {

    }
}
