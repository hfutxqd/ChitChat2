package com.room517.chitchat.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.Like;
import com.room517.chitchat.ui.activities.ExploreDetailActivity;
import com.room517.chitchat.ui.activities.ImageViewerActivity;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.adapters.ExploreListAdapter;
import com.room517.chitchat.ui.views.FloatingActionButton;
import com.room517.chitchat.utils.JsonUtil;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by imxqd on 2016/6/11.
 * 朋友圈列表Fragment
 */
public class ExploreListFragment extends BaseFragment implements ExploreListAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mList;
    private ExploreListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFab;

    public static ExploreListFragment newInstance(Bundle args) {
        ExploreListFragment exploreListFragment = new ExploreListFragment();
        exploreListFragment.setArguments(args);
        return exploreListFragment;
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
        RxBus.get().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_explore_list;
    }

    @Override
    protected void initMember() {
        RxBus.get().register(this);
        mAdapter = new ExploreListAdapter();
    }

    @Override
    protected void findViews() {
        mList = f(R.id.explore_list);
        mSwipeRefreshLayout = f(R.id.swipe_layout);
        mFab = ((MainActivity) getActivity()).getFab();
    }

    @Override
    protected void initUI() {
        mList.setAdapter(mAdapter);
    }

    @Override
    protected void setupEvents() {
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lmg = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lmg.findLastVisibleItemPosition() >= mAdapter.getItemCount() - 1) {
                    mAdapter.loadMore(new ExploreListAdapter.CallBack() {
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
            }
        });
        mAdapter.setOnItemClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mFab.attachToRecyclerView(mList);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });
    }


    @Subscribe(tags = {@Tag(Def.Event.ON_ACTIONBAR_CLICKED)})
    public void onActionBarClick(Object o) {
        if ((Integer) o == 1) {
            mList.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onLikeClick(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        if (item.isLiked()) {
            doUnLikeUI(item, itemView);
            doUnLike(item, itemView);
        } else {
            doLikeUI(item, itemView);
            doLike(item, itemView);
        }
    }

    @SuppressWarnings("deprecation")
    private void doLikeUI(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        if (!item.isLiked()) {
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

    private void doUnLike(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        RxHelper.ioMain(service.unlike(new Like(item.getId(), App.getMe().getId())),
                new SimpleObserver<ResponseBody>() {
                    @Override
                    public void onError(Throwable throwable) {
                        doLikeUI(item, itemView);
                        super.onError(throwable);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String json = responseBody.string();
                            if (!JsonUtil.getParam(json, "success").getAsBoolean()) {
                                doLikeUI(item, itemView);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @SuppressWarnings("deprecation")
    private void doUnLikeUI(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        if (item.isLiked()) {
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

    private void doLike(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        RxHelper.ioMain(service.like(new Like(item.getId(), App.getMe().getId())),
                new SimpleObserver<ResponseBody>() {
                    @Override
                    public void onError(Throwable throwable) {
                        doUnLikeUI(item, itemView);
                        super.onError(throwable);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String json = responseBody.string();
                            if (!JsonUtil.getParam(json, "success").getAsBoolean()) {
                                doUnLikeUI(item, itemView);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    @Override
    public void onCommentClick(Explore item, ExploreListAdapter.ExploreHolder itemView) {
        Intent intent = new Intent(getActivity(), ExploreDetailActivity.class);
        intent.putExtra("explore", item);
        intent.putExtra("isComment", true);
        startActivity(intent);
    }

    @Override
    public void onItemClick(Explore item) {
        Intent intent = new Intent(getActivity(), ExploreDetailActivity.class);
        intent.putExtra("explore", item);
        startActivity(intent);
    }

    @Override
    public void onImageClick(int pos, String[] urls) {
        Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
        intent.putExtra("pos", pos);
        intent.putExtra("urls", urls);
        startActivity(intent);
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
