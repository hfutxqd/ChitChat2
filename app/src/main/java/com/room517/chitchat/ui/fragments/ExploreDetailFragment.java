package com.room517.chitchat.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.NotificationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Comment;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.ImageViewerActivity;
import com.room517.chitchat.ui.adapters.CommentAdapter;
import com.room517.chitchat.ui.adapters.ExploreImagesAdapter;
import com.room517.chitchat.ui.adapters.ExploreListAdapter;
import com.room517.chitchat.utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by imxqd on 2016/6/11.
 * 朋友圈列表Fragment
 */
public class ExploreDetailFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, ExploreImagesAdapter.OnItemClickListener {

    private RecyclerView mCommentList;
    private CommentAdapter mCommentAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EditText mViewCommentText;
    private ImageButton mViewCommentSend;

    private ImageView mViewIcon, mViewLike;
    private TextView mViewNickname, mViewTime, mViewText, mViewLike_comment_count;
    private RecyclerView mViewImages;
    private ExploreImagesAdapter mImagesAdapter;

    private Explore mExplore;
    private String mExploreId = null;

    public static final String ARG_EXPLORE = "explore";
    public static final String ARG_IS_COMMENT = "isComment";
    public static final String ARG_EXPLORE_ID = Def.Key.EXPLORE_ID;

    public static ExploreDetailFragment newInstance(Explore args, boolean isComment) {
        ExploreDetailFragment exploreListFragment = new ExploreDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_EXPLORE, args);
        bundle.putBoolean(ARG_IS_COMMENT, isComment);
        exploreListFragment.setArguments(bundle);
        return exploreListFragment;
    }

    public static ExploreDetailFragment newInstance(String exploreId) {
        ExploreDetailFragment exploreListFragment = new ExploreDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_EXPLORE_ID, exploreId);
        exploreListFragment.setArguments(bundle);
        return exploreListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExplore = (Explore) getArguments().getSerializable(ARG_EXPLORE);
        mExploreId = getArguments().getString(ARG_EXPLORE_ID);
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
    protected int getLayoutRes() {
        return R.layout.fragment_explore_detail;
    }

    @Override
    protected void initMember() {
        mImagesAdapter = new ExploreImagesAdapter();
        mCommentAdapter = new CommentAdapter();
    }

    @Override
    protected void findViews() {
        mCommentList = f(R.id.detail_comment_list);
        mSwipeRefreshLayout = f(R.id.comment_swpie);
        mViewImages = f(R.id.detail_images);
        mViewCommentText = f(R.id.detail_comment_etxt);
        mViewCommentSend = f(R.id.detail_comment_send);
        mViewNickname = f(R.id.detail_nickname);
        mViewLike = f(R.id.detail_like);
        mViewTime = f(R.id.detail_time);
        mViewText= f(R.id.detail_text);
        mViewIcon= f(R.id.detail_icon);
        mViewLike_comment_count = f(R.id.detail_like_comment_count);
    }

    @Override
    protected void initUI() {
        mCommentList.setLayoutManager(new LinearLayoutManager(getContext()));
        mCommentList.setAdapter(mCommentAdapter);
        initExploreUI();
    }

    private void initEvents(){

        mViewLike.setOnClickListener(ExploreDetailFragment.this);
        mImagesAdapter.setOnItemClickListener(ExploreDetailFragment.this);
        mViewCommentSend.setOnClickListener(ExploreDetailFragment.this);
        mSwipeRefreshLayout.setOnRefreshListener(ExploreDetailFragment.this);
        if(getArguments().getBoolean(ARG_IS_COMMENT)) {
            mViewCommentText.requestFocus();
        }
    }

    @Override
    protected void setupEvents() {
        initEvents();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });
    }

    private void initExplore(final Callback callback){
        if(mExplore == null){
            Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
            ExploreService service = retrofit.create(ExploreService.class);
            mSwipeRefreshLayout.setRefreshing(true);
            RxHelper.ioMain(service.explore(mExploreId, App.getMe().getId()), new SimpleObserver<Explore>(){
                @Override
                public void onNext(Explore explore) {
                    mExplore = explore;
                    callback.onComplete();
                }
                @Override
                public void onError(Throwable throwable) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(),R.string.refresh_error, Toast.LENGTH_SHORT).show();
                    callback.onError();
                }
            });
        }else {
            callback.onComplete();
        }
    }

    private void initExploreUI(){
        if(mExplore != null){
            mImagesAdapter.setUrls(mExplore.getContent().getImages());
            mViewImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
            mViewImages.setAdapter(mImagesAdapter);
            mViewNickname.setText(mExplore.getNickname());
            mViewTime.setText(mExplore.getTime());
            if(mExplore.getContent().getText().trim().length() == 0) {
                mViewText.setVisibility(View.GONE);
            }else {
                mViewText.setText(mExplore.getContent().getText());
            }

            mViewLike_comment_count.setText(
                    getString(R.string.explore_like_comment_count, mExplore.getLike(),
                            mExplore.getComment_count()));

            User user = UserDao.getInstance().getUserById(mExplore.getDevice_id());
            Drawable icon;
            if(user == null) {
                icon = TextDrawable.builder()
                        .buildRound(mExplore.getNickname().substring(0, 1), mExplore.getColor());
            }else {
                icon = UserDao.getInstance().getUserById(mExplore.getDevice_id()).getAvatarDrawable();
            }
            mViewIcon.setImageDrawable(icon);

            boolean isLiked = mExplore.isLiked();
            if(isLiked) {
                mViewLike.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
            }else {
                mViewLike.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
            }

            int like = mExplore.getLike();
            int comment = mExplore.getComment_count();
            mViewLike_comment_count.setText(
                    getString(R.string.explore_like_comment_count, like, comment));
        }
    }

    private void initComments(){
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService exploreService = retrofit.create(ExploreService.class);
        RxHelper.ioMain(exploreService.ListComment(mExplore.getId())
                , new SimpleObserver<ArrayList<Comment>>()
                {
                    @Override
                    public void onNext(ArrayList<Comment> comments) {
                        mCommentAdapter.set(comments);
                        mCommentAdapter.notifyDataSetChanged();
                        if(mExploreId != null){
                            NotificationHelper.clearUnreadCommentsCount(mExploreId);
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        initExplore(new Callback() {
            @Override
            public void onComplete() {
                initExploreUI();
                initComments();
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_comment_send:
                doComment();
                break;
            case R.id.detail_like:

                break;

        }
    }

    private void doComment() {
        String text = mViewCommentText.getText().toString();
        if(text.length() > 0) {
            mViewCommentSend.setClickable(false);
            Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
            ExploreService exploreService = retrofit.create(ExploreService.class);
            RxHelper.ioMain(exploreService.comment(new Comment(
                    mExplore.getId(),App.getMe().getId(),App.getMe().getName()
                    , text, "", App.getMe().getColor())), new SimpleObserver<ResponseBody>() {
                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(getContext(), R.string.comment_error, Toast.LENGTH_SHORT)
                            .show();
                    mViewCommentSend.setClickable(true);
                }

                @Override
                public void onNext(ResponseBody responseBody) {
                    try {
                        String json = responseBody.string();
                        if(JsonUtil.getParam(json, "success").getAsBoolean())
                        {
                            Toast.makeText(getContext(), R.string.comment_success, Toast.LENGTH_SHORT)
                                    .show();
                            mViewCommentText.setText("");
                            onRefresh();
                        }else {
                            Toast.makeText(getContext(), R.string.comment_error_server, Toast.LENGTH_SHORT)
                                    .show();
                        }
                        mViewCommentSend.setClickable(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    @Override
    public void onItemClick(int pos) {
        Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
        intent.putExtra("pos", pos);
        intent.putExtra("urls", mExplore.getContent().getImages());
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    private void doLikeUI(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        if(!item.isLiked()) {
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

    @SuppressWarnings("deprecation")
    private void doUnLikeUI(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        if(item.isLiked()) {
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

    private interface Callback{
        void onComplete();
        void onError();
    }
}
