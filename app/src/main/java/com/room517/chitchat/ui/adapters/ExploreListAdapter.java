package com.room517.chitchat.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amulyakhare.textdrawable.TextDrawable;
import com.room517.chitchat.App;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.ListExploreResult;
import com.room517.chitchat.model.Pager;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.LocationInforActivity;
import com.room517.chitchat.ui.views.ExpandableTextView;
import com.room517.chitchat.ui.views.LocationLayout;
import com.room517.chitchat.utils.DateTimeUtil;
import com.room517.chitchat.utils.LocationUtil;

import java.util.ArrayList;

import retrofit2.Retrofit;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈动态列表的适配器
 */
public class ExploreListAdapter extends RecyclerView.Adapter<ExploreListAdapter.ExploreHolder> {
    private ArrayList<Explore> mList;
    private static final int TYPE_NORMAL = 2;
    private static final int TYPE_FOOTER = 3;

    private Pager pager = new Pager();
    private boolean showUser = false;
    private boolean isDetail = false;
    private User user;
    private AMapLocation location;

    public ExploreListAdapter() {
        mList = new ArrayList<>();
    }

    public ExploreListAdapter(User user, boolean isDetail) {
        showUser = true;
        if(user.getId().equals(App.getMe().getId())) {
            showUser = false;
        } else {
            showUser = true;
            this.user = user;
        }
        this.isDetail = isDetail;
        mList = new ArrayList<>();
    }

    public void set(ArrayList<Explore> list) {
        mList.clear();
        mList.addAll(list);
    }

    public void add(ArrayList<Explore> list) {
        mList.addAll(list);
    }

    @Override
    public ExploreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            return new ExploreHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.explore_list_footer, parent, false), viewType);
        } else {
            return new ExploreHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.explore_item, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int last = mList.size();
        if (position == last) {
            return TYPE_FOOTER;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }


    @Override
    public void onBindViewHolder(final ExploreHolder holder,int postion) {
        if (holder.viewType == TYPE_FOOTER ) {
            if (postion == 1) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                if (pager.getCurrent_page() == pager.getTotal_page()) {
                    holder.itemView.setVisibility(View.GONE);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                }
            }
            return;
        }
        Context context = holder.nickname.getContext();
        String text = mList.get(postion).getContent().getText();
        String nickname = mList.get(postion).getNickname();
        String time = DateTimeUtil.formatDatetime(mList.get(postion).getTime());
        String deviceId = mList.get(postion).getDevice_id();
        final String[] images = mList.get(postion).getContent().getImages();
        boolean isLiked = mList.get(postion).isLiked();
        int color = mList.get(postion).getColor();
        ExploreImagesAdapter adapter = new ExploreImagesAdapter(images, isDetail);
        adapter.setOnItemClickListener(new ExploreImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View view) {
                mOnItemClickListener.onImageClick(pos, images, view);
            }
        });
        holder.setUser(nickname, deviceId, color);
        holder.time.setText(time);
        holder.setText(text);
        holder.setLikeCount(mList.get(postion).getLike());
        holder.setCommentCount(mList.get(postion).getComment_count());
        holder.setLocation(mList.get(postion));
        holder.setLike(isLiked);
        if (images.length <= 1) {
            holder.images.setLayoutManager(new LinearLayoutManager(context));
        } else {
            GridLayoutManager layoutManager = new GridLayoutManager(holder.text.getContext(), 3);
            holder.images.setLayoutManager(layoutManager);
        }
        holder.images.setAdapter(adapter);

        // OnClick在此处无效,故采用OnTouch方式
        holder.images.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mOnItemClickListener.onItemClick(mList.get(holder.getAdapterPosition()));
                }
                return true;
            }
        });
        View.OnClickListener likeClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onLikeClick(mList.get(holder.getAdapterPosition()), holder);
            }
        };
        holder.like.setOnClickListener(likeClicked);
        holder.like_count.setOnClickListener(likeClicked);

        View.OnClickListener commentClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onCommentClick(mList.get(holder.getAdapterPosition()), holder);
            }
        };
        holder.comment.setOnClickListener(commentClicked);
        holder.comment_count.setOnClickListener(commentClicked);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(mList.get(holder.getAdapterPosition()));
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }



    public synchronized void refresh(final CallBack callBack) {
        callBack.onStart();
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService exploreService = retrofit.create(ExploreService.class);
        location = App.getLocationHelper().getLastKnownLocation();
        if(showUser) {
            RxHelper.ioMain(exploreService.exploreByPager("1", user.getId())
                    , new SimpleObserver<ListExploreResult>() {
                        @Override
                        public void onError(Throwable throwable) {
                            callBack.onError(throwable);
                        }

                        @Override
                        public void onNext(ListExploreResult result) {
                            set(result.getData());
                            pager = result.getPager();
                            notifyDataSetChanged();
                            callBack.onComplete();
                        }
                    });
        } else {
            if (location == null) {
                location = new AMapLocation("");
                location.setLatitude(0);
                location.setLongitude(0);
            }
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            RxHelper.ioMain(exploreService.ListExploreByPager("1", App.getMe().getId()
                    , latitude, longitude)
                    , new SimpleObserver<ListExploreResult>() {
                        @Override
                        public void onError(Throwable throwable) {
                            callBack.onError(throwable);
                        }

                        @Override
                        public void onNext(ListExploreResult result) {
                            set(result.getData());
                            pager = result.getPager();
                            notifyDataSetChanged();
                            callBack.onComplete();
                        }
                    });
        }
    }

    boolean isLoading = false;

    public synchronized void loadMore(final @Nullable CallBack callBack) {
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService exploreService = retrofit.create(ExploreService.class);
        location = App.getLocationHelper().getLastKnownLocation();
        if (!isLoading && pager.getTotal_page() > pager.getCurrent_page()) {
            isLoading = true;
            if(showUser) {
                if (callBack != null) {
                    callBack.onStart();
                }
                RxHelper.ioMain(exploreService.exploreByPager(String.valueOf(pager.getNext_page()),
                        user.getId()),
                        new SimpleObserver<ListExploreResult>() {
                            @Override
                            public void onError(Throwable throwable) {
                                isLoading = false;
                                if (callBack != null) {
                                    callBack.onError(throwable);
                                }
                            }

                            @Override
                            public void onNext(ListExploreResult result) {
                                isLoading = false;
                                if (result.getData().size() > 0) {
                                    int pos = getItemCount() - 1;
                                    pager = result.getPager();
                                    add(result.getData());
                                    notifyItemRangeInserted(pos, result.getData().size());
                                    if (callBack != null) {
                                        callBack.onComplete();
                                    }
                                }
                            }
                        });
            } else {
                if (location == null) {
                    User me = App.getMe();
                    location = new AMapLocation("");
                    location.setLatitude(me.getLatitude());
                    location.setLongitude(me.getLongitude());
                }
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                if (callBack != null) {
                    callBack.onStart();
                }
                RxHelper.ioMain(exploreService.ListExploreByPager(String.valueOf(pager.getNext_page()),
                        App.getMe().getId(), latitude, longitude),
                        new SimpleObserver<ListExploreResult>() {
                            @Override
                            public void onError(Throwable throwable) {
                                isLoading = false;
                                if (callBack != null) {
                                    callBack.onError(throwable);
                                }
                            }

                            @Override
                            public void onNext(ListExploreResult result) {
                                isLoading = false;
                                if (result.getData().size() > 0) {
                                    int pos = getItemCount() - 1;
                                    pager = result.getPager();
                                    add(result.getData());
                                    notifyItemRangeInserted(pos, result.getData().size());
                                    if (callBack != null) {
                                        callBack.onComplete();
                                    }
                                }
                            }
                        });
            }

        } else {
            if (callBack != null) {
                callBack.onComplete();
            }
        }
    }

    private OnItemClickListener mOnItemClickListener = null;

    public class ExploreHolder extends RecyclerView.ViewHolder {
        public LocationLayout locationLayout;
        public ImageView icon, like, comment;
        public TextView nickname, text, time, like_count, comment_count, distance;
        public RecyclerView images;
        public int viewType;

        public ExploreHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
        }

        public ExploreHolder(View itemView) {
            super(itemView);
            viewType = TYPE_NORMAL;
            icon = (ImageView) itemView.findViewById(R.id.explore_item_icon);
            like = (ImageView) itemView.findViewById(R.id.explore_item_like);
            comment = (ImageView) itemView.findViewById(R.id.explore_item_comment);
            nickname = (TextView) itemView.findViewById(R.id.explore_item_nickname);
            time = (TextView) itemView.findViewById(R.id.explore_item_time);
            text = (TextView) itemView.findViewById(R.id.explore_item_text);
            like_count = (TextView) itemView.findViewById(R.id.explore_item_like_count);
            comment_count = (TextView) itemView.findViewById(R.id.explore_item_comment_count);
            distance = (TextView) itemView.findViewById(R.id.explore_item_distance);
            locationLayout = (LocationLayout) itemView.findViewById(R.id.explore_location);
            images = (RecyclerView) itemView.findViewById(R.id.explore_item_images);
            images.setNestedScrollingEnabled(false);
        }

        public void setLikeCount(int count) {
            if (count == 0) {
                like_count.setText(itemView.getContext().getString(R.string.like));
            } else {
                like_count.setText(String.valueOf(count));
            }
        }

        public void setCommentCount(int count) {
            if (count == 0) {
                comment_count.setText(itemView.getContext().getString(R.string.comment));
            } else {
                comment_count.setText(String.valueOf(count));
            }
        }

        public void setLocation(final Explore explore) {
            if (explore.getLatitude() == 0 && explore.getLongitude() == 0) {
                locationLayout.setVisibility(View.GONE);
                distance.setVisibility(View.GONE);
            } else {
                locationLayout.setVisibility(View.VISIBLE);
                distance.setVisibility(View.VISIBLE);
                locationLayout.setText(explore.getLocationAddr());
                locationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(App.getApp(), LocationInforActivity.class);
                        intent.putExtra(LocationInforActivity.ARG_TITLE, explore.getLocationAddr());
                        intent.putExtra(LocationInforActivity.ARG_LATITUDE, explore.getLatitude());
                        intent.putExtra(LocationInforActivity.ARG_LONGITUDE, explore.getLongitude());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.getApp().startActivity(intent);
                    }
                });
                if(location != null) {
                    double d =
                            LocationUtil.getDistance(location.getLatitude(), location.getLongitude()
                                    , explore.getLatitude(), explore.getLongitude());
                    if (d < 10) {
                        distance.setText(R.string.location_apart_close);
                    } else {
                        distance.setText(App.getApp().getString(R.string.location_apart,
                                LocationUtil.distanceToString(d)));
                    }

                }
            }
        }

        public void setLike(boolean isLiked) {
            if (isLiked) {
                like.setImageDrawable(itemView.getContext().getResources()
                        .getDrawable(R.drawable.ic_favorite_black_24dp));
            } else {
                like.setImageDrawable(itemView.getContext().getResources()
                        .getDrawable(R.drawable.ic_favorite_border_black_24dp));
            }
        }

        public void setText(String str) {
            if (str.trim().length() == 0) {
                text.setVisibility(View.GONE);
            } else {
                text.setVisibility(View.VISIBLE);
                text.setText(str);
            }
        }

        public void setUser(final String nickname,final String deviceId,final int color) {
            final User user = UserDao.getInstance().getUserById(deviceId);
            Drawable icon;
            if (user == null) {
                icon = TextDrawable.builder()
                        .buildRound(nickname.substring(0, 1), color);
                this.icon.setImageDrawable(icon);
                this.nickname.setText(nickname);
            } else {
                icon = UserDao.getInstance().getUserById(deviceId).getAvatarDrawable();
                String nicknameTmp = UserDao.getInstance().getUserById(deviceId).getName();
                this.icon.setImageDrawable(icon);
                this.nickname.setText(nicknameTmp);
            }

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(user == null) {
                        User tmp = new User(deviceId, nickname, User.SEX_PRIVATE,
                                String.valueOf(color), "", 0 ,0, 0);
                        mOnItemClickListener.onUserClick(tmp);
                    } else {
                        mOnItemClickListener.onUserClick(user);
                    }
                }
            };
            this.icon.setOnClickListener(listener);
            this.nickname.setOnClickListener(listener);
        }
    }

    public interface CallBack {
        void onStart();

        void onError(Throwable throwable);

        void onComplete();
    }

    public interface OnItemClickListener {
        void onLikeClick(Explore item, ExploreHolder itemView);

        void onUserClick(User user);

        void onCommentClick(Explore item, ExploreHolder itemView);

        void onItemClick(Explore item);

        void onImageClick(int pos, String[] urls, View view);
    }
}
