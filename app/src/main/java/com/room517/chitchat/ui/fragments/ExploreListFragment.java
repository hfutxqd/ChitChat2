package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.room517.chitchat.App;
import com.room517.chitchat.R;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.Like;
import com.room517.chitchat.ui.adapters.ExploreListAdapter;
import com.room517.chitchat.utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by ywwynm on 2016/5/24.
 * 朋友圈列表Fragment
 */
public class ExploreListFragment extends BaseFragment implements ExploreListAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mList;
    private ExploreListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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
    }

    @Override
    protected void findViews() {
        mList = f(R.id.explore_list);
        mSwipeRefreshLayout = f(R.id.swipe_layout);
    }

    @Override
    protected void initUI() {
        mList.setAdapter(mAdapter);
    }

    @Override
    protected void setupEvents() {
        mAdapter.setOnItemClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });
    }

    @Override
    public void onLikeClick(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {

        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        if(item.isLiked())
        {
            doUnLikeUI(item, itemView);
            RxHelper.ioMain(service.unlike(new Like(item.getId(), App.getMe().getId())),
                    new SimpleObserver<ResponseBody>()
                    {
                        @Override
                        public void onError(Throwable throwable) {
                            doLikeUI(item, itemView);
                            super.onError(throwable);
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                String json = responseBody.string();
                                if(!JsonUtil.getParam(json, "success").getAsBoolean())
                                {
                                    doLikeUI(item, itemView);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }else {
            doLikeUI(item, itemView);
            RxHelper.ioMain(service.like(new Like(item.getId(), App.getMe().getId())),
                    new SimpleObserver<ResponseBody>()
                    {
                        @Override
                        public void onError(Throwable throwable) {
                            doUnLikeUI(item, itemView);
                            super.onError(throwable);
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                String json = responseBody.string();
                                if(!JsonUtil.getParam(json, "success").getAsBoolean())
                                {
                                    doUnLikeUI(item, itemView);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void doLikeUI(final Explore item, final ExploreListAdapter.ExploreHolder itemView)
    {
        if(!item.isLiked())
        {
            item.setLiked(true);
            item.setLike(item.getLike() + 1);
            itemView.like_comment_count.setText(
                    getString(R.string.explore_like_comment_count,
                            item.getLike(), item.getComment_count())
            );
            itemView.like.setImageDrawable(getResources()
                    .getDrawable(R.drawable.ic_favorite_black_24dp));
        }
    }

    private void doUnLikeUI(final Explore item, final ExploreListAdapter.ExploreHolder itemView)
    {
        if(item.isLiked())
        {
            item.setLiked(false);
            item.setLike(item.getLike() - 1);
            itemView.like_comment_count.setText(
                    getString(R.string.explore_like_comment_count,
                            item.getLike(), item.getComment_count())
            );
            itemView.like.setImageDrawable(getResources()
                    .getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }
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

    @Override
    public void onRefresh() {
        mAdapter.refresh(new ExploreListAdapter.CallBack() {
            @Override
            public void onStart() {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onError(Throwable throwable) {
                mSwipeRefreshLayout.setRefreshing(false);
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                mSwipeRefreshLayout.setRefreshing(false);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
