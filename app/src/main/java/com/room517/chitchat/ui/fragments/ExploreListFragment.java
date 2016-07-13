package com.room517.chitchat.ui.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.Like;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.ExploreDetailActivity;
import com.room517.chitchat.ui.activities.ImageViewerActivity;
import com.room517.chitchat.ui.activities.UserExploreActivity;
import com.room517.chitchat.ui.adapters.ExploreListAdapter;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;
import com.room517.chitchat.utils.DisplayUtil;
import com.room517.chitchat.utils.ImageCompress;
import com.room517.chitchat.utils.JsonUtil;
import com.room517.chitchat.utils.ViewAnimationUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import rx.Observable;
import xyz.imxqd.photochooser.constant.Constant;

/**
 * Created by imxqd on 2016/6/11.
 * 朋友圈列表Fragment
 */
public class ExploreListFragment extends BaseFragment implements ExploreListAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {
    public static final String ARG_SHOW_SELF = "show_user";
    public static final String ARG_USER = "user";

    private static final int REQUEST_PICK_PHOTO = 1;

    private RecyclerView mList;
    private ExploreListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar toolbar;
    private ImageView userIcon;
    private AppBarLayout appBarLayout;
    private ImageView header;

    private boolean showUser = false;
    private User user;
    private DisplayImageOptions options;


    private LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext()) {
        @Override
        public void smoothScrollToPosition(final RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        protected int calculateTimeForScrolling(int dx) {
                            if (dx >= 2000) {
                                return 80;
                            }
                            return 50;
                        }

                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return mLayoutManager.computeScrollVectorForPosition(targetPosition);
                        }
                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    };


    public static ExploreListFragment newInstance(Bundle args) {
        ExploreListFragment exploreListFragment = new ExploreListFragment();
        exploreListFragment.setArguments(args);
        return exploreListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle data = getArguments();
        if( data != null) {
            showUser = data.getBoolean(ARG_SHOW_SELF, false);
            user = data.getParcelable(ARG_USER);
            User tmp = UserDao.getInstance().getUserById(user.getId());
            if(tmp == null) {
                UserDao.getInstance().insert(user);
            } else {
                user = tmp;
            }
        }
        if(!showUser) {
            user = App.getMe();
            RxBus.get().register(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (!showUser) {
            RxBus.get().unregister(this);
        }
        super.onDestroyView();
    }

    @Override
    protected int getLayoutRes() {
        if(showUser) {
            return R.layout.fragment_explore_user_collapsing;
        } else {
            return R.layout.fragment_explore_collapsing;
        }
    }

    @Override
    protected void initMember() {
        mAdapter = new ExploreListAdapter(user, false);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(null)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        getUserCover();
    }

    @Override
    protected void findViews() {
        mList = f(R.id.explore_list);
        mSwipeRefreshLayout = f(R.id.swipe_layout);
        appBarLayout = f(R.id.app_bar);
        header = f(R.id.explore_header_bg_image);
        if(showUser) {
            toolbar = f(R.id.toolbar);
        } else {
            userIcon = f(R.id.fab);
        }

    }

    @Override
    protected void initUI() {
        mList.setLayoutManager(mLayoutManager);
        mList.setAdapter(mAdapter);
        if(!TextUtils.isEmpty(user.getCoverUrl())) {
            ImageLoader.getInstance().displayImage(user.getCoverUrl(), header, options);
        }
        if(showUser) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(user.getName());
            }
        } else {
            userIcon.setImageDrawable(App.getMe().getAvatarDrawable());
        }
    }

    @Override
    protected void setupEvents() {
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) {
                    RxBus.get().post(Def.Event.HIDE_FAB_TO_BOTTOM, new Object());
                } else if (dy < 0) {
                    RxBus.get().post(Def.Event.SHOW_FAB_FROM_BOTTOM, new Object());
                }
                LinearLayoutManager lmg = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lmg.findLastVisibleItemPosition() >= mAdapter.getItemCount() - 1) {
                    mAdapter.loadMore(null);
                }
            }
        });
        if(!showUser) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    int px = DisplayUtil.dp2px(28);
                    if(verticalOffset < -px) {
                        ViewAnimationUtil.scaleOut(userIcon, new ViewAnimationUtil.Callback(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                userIcon.setClickable(false);
                            }
                        });
                    } else if (verticalOffset >= -px){
                        ViewAnimationUtil.scaleIn(userIcon, new ViewAnimationUtil.Callback(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                userIcon.setClickable(true);
                            }
                        });
                    }
                }
            });
            userIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserExploreActivity.class);
                    intent.putExtra(UserExploreActivity.ARG_USER, App.getMe());
                    startActivity(intent);
                }
            });

            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final SimpleListDialog dialog = new SimpleListDialog();
                    dialog.setItems(getResources().getStringArray(R.array.header_menu));
                    ArrayList<View.OnClickListener> listeners = new ArrayList<>(1);
                    listeners.add(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Intent intent = new Intent("com.room517.chitchat.action.CHOSE_PHOTOS");
                            intent.putExtra(Constant.EXTRA_PHOTO_LIMIT, 1);
                            startActivityForResult(intent, REQUEST_PICK_PHOTO);
                        }
                    });
                    dialog.setOnItemClickListeners(listeners);
                    dialog.show(getActivity().getFragmentManager(), "explore_header");
                }
            });

        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PICK_PHOTO) {
            if(resultCode == Activity.RESULT_OK) {
                final ArrayList<String> images = data.getStringArrayListExtra(Constant.EXTRA_PHOTO_PATHS);
                ImageLoader.getInstance().displayImage("file://"+images.get(0), header);
                startSetupUserCover(images.get(0));
            }
        }
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_ACTIONBAR_CLICKED)})
    public void onActionBarClick(Object o) {
        if ((Integer) o == 1) {
            appBarLayout.setExpanded(true, true);
            mLayoutManager.smoothScrollToPosition(mList, null, 0);
        }
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_EXPLORE_SELF_ICON_CLICKED)})
    public void onSelfIconClick(Object o) {
        Intent intent = new Intent(getActivity(), UserExploreActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLikeClick(final Explore item, final ExploreListAdapter.ExploreHolder itemView) {
        Observable<ResponseBody> observable;
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        doLikeLocal(item, itemView);
        if (item.isLiked()) {
            observable = service.like(new Like(item.getId(), App.getMe().getId()));
        } else {
            observable = service.unlike(new Like(item.getId(), App.getMe().getId()));
        }
        RxHelper.ioMain(observable, new SimpleObserver<ResponseBody>() {
            @Override
            public void onError(Throwable throwable) {
                doLikeLocal(item, itemView);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String json = responseBody.string();
                    if (!JsonUtil.getParam(json, "success").getAsBoolean()) {
                        doLikeLocal(item, itemView);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    responseBody.close();
                }
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        if(!showUser) {
            Intent intent = new Intent(getActivity(), UserExploreActivity.class);
            intent.putExtra(UserExploreActivity.ARG_USER, user);
            startActivity(intent);
        }
    }

    private void doLikeLocal(Explore item, ExploreListAdapter.ExploreHolder itemView) {
        if (item.isLiked()) {
            item.setLiked(false);
            item.setLike(item.getLike() - 1);
        } else {
            item.setLiked(true);
            item.setLike(item.getLike() + 1);
        }
        itemView.setLikeCount(item.getLike());
        itemView.setLike(item.isLiked());
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
    public void onImageClick(int pos, String[] urls, View view) {
        Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
        intent.putExtra("pos", pos);
        intent.putExtra("urls", urls);
        ActivityOptionsCompat animation =
                ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0
                        , view.getWidth(), view.getHeight());
        ActivityCompat.startActivity(getActivity(), intent, animation.toBundle());
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
            }
        });
    }

    public void getUserCover() {
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        RxHelper.ioMain(service.getCover(user.getId()), new SimpleObserver<ResponseBody>()
        {
            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String json = responseBody.string();
                    Log.d("getUserCover", json);
                    if (JsonUtil.getParam(json, "success").getAsBoolean()) {
                        String url = JsonUtil.getParam(json, "url").getAsString();
                        if(!url.equals(user.getCoverUrl())) {
                            user.setCoverUrl(url);
                            UserDao.getInstance().update(user);
                            ImageLoader.getInstance().displayImage(url, header, options);
                        }
                    } else {
                        header.setImageResource(R.drawable.explore_list_hearder_bg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startSetupUserCover(final String path) {
        compressImage(path);
    }

    public void compressImage(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tmp = ImageCompress.compress(path);
                RxBus.get().post(Def.Event.ON_COMPRESS_IMAGE_COMPLETE, tmp);
            }
        }).start();
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_COMPRESS_IMAGE_COMPLETE)})
    public void onCompressImageComplete(final String path) {
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        RxHelper.ioMain(service.upload(path,
                RequestBody.create(MediaType.parse("multipart/form-data"), new File(path)))
                , new SimpleObserver<ResponseBody>(){
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String json = responseBody.string();
                            if (JsonUtil.getParam(json, "success").getAsBoolean()) {
                                String url = JsonUtil.getParam(json, "url").getAsString();
                                RxBus.get().post(Def.Event.ON_FILE_UPLOAD_COMPLETE, url);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            RxBus.get().post(Def.Event.ON_FILE_UPLOAD_FAIL, path);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        RxBus.get().post(Def.Event.ON_FILE_UPLOAD_FAIL, path);
                    }
                });
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_FILE_UPLOAD_FAIL)})
    public void onFileUploadFail(final String path) {
        RxBus.get().post(Def.Event.HIDE_FAB_TO_BOTTOM, new Object());
        Snackbar.make(getView(), R.string.upload_user_cover_error, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.upload_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RxBus.get().post(Def.Event.ON_COMPRESS_IMAGE_COMPLETE, path);
                    }
                })
                .show();
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_FILE_UPLOAD_COMPLETE)})
    public void onFileUploadComplete(final String url) {
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService service = retrofit.create(ExploreService.class);
        RxHelper.ioMain(service.setCover(user.getId(), url)
                , new SimpleObserver<ResponseBody>(){
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String json = responseBody.string();
                            if (JsonUtil.getParam(json, "success").getAsBoolean()) {
                                RxBus.get().post(Def.Event.ON_SET_COVER_SUCCESS, url);
                            } else {
                                RxBus.get().post(Def.Event.ON_SET_COVER_FAIL, url);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            RxBus.get().post(Def.Event.ON_SET_COVER_FAIL, url);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        RxBus.get().post(Def.Event.ON_SET_COVER_FAIL, url);
                    }
                });
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_SET_COVER_SUCCESS)})
    public void onSetCoverSuccess(String url) {
        user.setCoverUrl(url);
        UserDao.getInstance().update(user);
        ImageLoader.getInstance().displayImage(url, header, options);
    }

    @Subscribe(tags = {@Tag(Def.Event.ON_SET_COVER_FAIL)})
    public void onSetCoverFail(String url) {
        Snackbar.make(getView(), R.string.set_user_cover_error, Snackbar.LENGTH_LONG)
                .show();
    }
}
