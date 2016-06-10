package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.room517.chitchat.R;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.ui.adapters.ExploreListAdapter;

import java.util.ArrayList;

import retrofit2.Retrofit;

/**
 * Created by ywwynm on 2016/5/24.
 * 朋友圈列表Fragment
 */
public class ExploreListFragment extends BaseFragment implements ExploreListAdapter.OnItemClickListener {

    private RecyclerView mList;
    private ExploreListAdapter mAdapter;

    public static ExploreListFragment newInstance(Bundle args) {
        ExploreListFragment exploreListFragment = new ExploreListFragment();
        exploreListFragment.setArguments(args);
        return exploreListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        super.init();
        return mContentView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_explore_list;
    }

    @Override
    protected void initMember() {
        mAdapter = new ExploreListAdapter();
        mAdapter.refresh(new ExploreListAdapter.CallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                mAdapter.notifyDataSetChanged();
            }
        });
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
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onLikeClick(Explore item, ExploreListAdapter.ExploreHolder itemView) {
        System.out.println("onLikeClick");
    }

    @Override
    public void onCommentClick(Explore item, ExploreListAdapter.ExploreHolder itemView) {
        System.out.println("onCommentClick");
    }

    @Override
    public void onItemClick(Explore item) {
        System.out.println("onItemClick");
    }

    @Override
    public void onImageClick(int pos, String[] urls) {
        System.out.println("onImageClick : "+ pos);
    }
}
