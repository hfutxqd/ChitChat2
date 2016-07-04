package com.room517.chitchat.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.room517.chitchat.App;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.OpenMapHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.views.LocationLayout;
import com.room517.chitchat.utils.DateTimeUtil;

import java.util.ArrayList;

import retrofit2.Retrofit;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈动态列表的适配器
 */
public class ExploreListAdapter extends RecyclerView.Adapter<ExploreListAdapter.ExploreHolder> {
    private ArrayList<Explore> mList;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_NORMAL = 2;
    private static final int TYPE_FOOTER = 3;

    public ExploreListAdapter() {
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
        if(viewType == TYPE_HEADER){
            return new ExploreHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.explore_list_header, parent, false), viewType);

        }else if(viewType == TYPE_FOOTER){
            return new ExploreHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.explore_list_footer, parent, false), viewType);
        }else {
            return new ExploreHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.explore_item, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int last = mList.size() + 1;
        if(position == 0){
            return TYPE_HEADER;
        }else if(position == last){
            return TYPE_FOOTER;
        }else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() + 2;
    }



    @Override
    public void onBindViewHolder(final ExploreHolder holder,final int postion) {
        if(holder.viewType == TYPE_FOOTER || holder.viewType == TYPE_HEADER){
            if(holder.viewType == TYPE_FOOTER && postion == 1){
                holder.itemView.setVisibility(View.GONE);
            }else if(holder.viewType == TYPE_FOOTER){
                holder.itemView.setVisibility(View.VISIBLE);
            }else{
                holder.icon.setImageDrawable(App.getMe().getAvatarDrawable());
                holder.nickname.setText(App.getMe().getName());
            }
            return;
        }
        final int pos = postion - 1;
//        System.out.println("id---->" + mList.get(pos).getId());
        Context context = holder.nickname.getContext();
        String text = mList.get(pos).getContent().getText();
        String nickname = mList.get(pos).getNickname();
        String time = DateTimeUtil.formatDatetime(mList.get(pos).getTime());
        String deviceId = mList.get(pos).getDevice_id();
        final String[] images = mList.get(pos).getContent().getImages();
        boolean isLiked = mList.get(pos).isLiked();
        int color = mList.get(pos).getColor();
        ExploreImagesAdapter adapter = new ExploreImagesAdapter(images);
        adapter.setOnItemClickListener(new ExploreImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View view) {
                mOnItemClickListener.onImageClick(pos, images, view);
            }
        });
        holder.setUser(nickname, deviceId, color);
        holder.time.setText(time);
        holder.setText(text);
        holder.setLikeCount(mList.get(pos).getLike());
        holder.setCommentCount(mList.get(pos).getComment_count());
        holder.setLocation(mList.get(pos).getContent().getLocation());
        holder.setLike(isLiked);
        if(images.length <= 1){
            holder.images.setLayoutManager(new LinearLayoutManager(context));
        }else {
            holder.images.setLayoutManager(new GridLayoutManager(holder.text.getContext(), 3));
        }
        holder.images.setAdapter(adapter);

        // OnClick在此处无效,故采用OnTouch方式
        holder.images.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mOnItemClickListener.onItemClick(mList.get(pos));
                }
                return true;
            }
        });
        View.OnClickListener likeClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onLikeClick(mList.get(pos), holder);
            }
        };
        holder.like.setOnClickListener(likeClicked);
        holder.like_count.setOnClickListener(likeClicked);

        View.OnClickListener commentClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onCommentClick(mList.get(pos), holder);
            }
        };
        holder.comment.setOnClickListener(commentClicked);
        holder.comment_count.setOnClickListener(commentClicked);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(mList.get(pos));
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
        RxHelper.ioMain(exploreService.ListExplore("0", App.getMe().getId())
                , new SimpleObserver<ArrayList<Explore>>() {
                    @Override
                    public void onError(Throwable throwable) {
                        callBack.onError(throwable);
                    }

                    @Override
                    public void onNext(ArrayList<Explore> explores) {
                        set(explores);
                        notifyDataSetChanged();
                        callBack.onComplete();
                    }
                });
    }

    boolean isLoading = false;

    public synchronized void loadMore(final CallBack callBack) {
//        System.out.println("lastId----------------------------------------->");
        if (!isLoading && mList.size() > 0) {
            isLoading = true;
//            System.out.println("lastId:" + mList.get(mList.size() - 1).getId());
            callBack.onStart();
            Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
            ExploreService exploreService = retrofit.create(ExploreService.class);
            RxHelper.ioMain(exploreService.ListExplore(mList.get(mList.size() - 1).getId(),
                    App.getMe().getId()),
                    new SimpleObserver<ArrayList<Explore>>() {
                        @Override
                        public void onError(Throwable throwable) {
                            isLoading = false;
                            callBack.onError(throwable);
                        }

                        @Override
                        public void onNext(ArrayList<Explore> explores) {
                            isLoading = false;
                            if (explores.size() > 0) {
                                int pos = getItemCount() - 1;
                                add(explores);
                                notifyItemRangeInserted(pos, explores.size());
                                callBack.onComplete();
                            }
                        }
                    });
        }
    }

    private OnItemClickListener mOnItemClickListener = null;

    public class ExploreHolder extends RecyclerView.ViewHolder {
        public LocationLayout locationLayout;
        public ImageView icon, like, comment;
        public TextView nickname, time, text, like_count, comment_count;
        public RecyclerView images;
        public int viewType;

        public ExploreHolder(View itemView, int viewType){
            super(itemView);
            this.viewType = viewType;
            if(viewType == TYPE_HEADER){
                nickname = (TextView) itemView.findViewById(R.id.explore_header_nickname);
                icon = (ImageView) itemView.findViewById(R.id.explore_header_icon);
                nickname.setText(App.getMe().getName());
                icon.setImageDrawable(App.getMe().getAvatarDrawable());
            }
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
            locationLayout = (LocationLayout) itemView.findViewById(R.id.explore_location);
            images = (RecyclerView) itemView.findViewById(R.id.explore_item_images);
        }

        public void setLikeCount(int count){
            if(count == 0){
                like_count.setText(itemView.getContext().getString(R.string.like));
            }else {
                like_count.setText(String.valueOf(count));
            }
        }

        public void setCommentCount(int count){
            if(count == 0){
                comment_count.setText(itemView.getContext().getString(R.string.comment));
            }else {
                comment_count.setText(String.valueOf(count));
            }
        }

        public void setLocation(final Explore.Location location){
            if(location.getLatitude() == 0 && location.getLongitude() == 0){
                locationLayout.setVisibility(View.GONE);
            }else {
                locationLayout.setVisibility(View.VISIBLE);
                locationLayout.setText(location.getAddrName());
                locationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OpenMapHelper.open(location.getLongitude()
                                , location.getLatitude(), location.getAddrName());
                    }
                });
            }
        }

        public void setLike(boolean isLiked){
            if(isLiked){
                like.setImageDrawable(itemView.getContext().getResources()
                        .getDrawable(R.drawable.ic_favorite_black_24dp));
            }else {
                like.setImageDrawable(itemView.getContext().getResources()
                        .getDrawable(R.drawable.ic_favorite_border_black_24dp));
            }
        }

        public void setText(String str){
            if (str.trim().length() == 0) {
                text.setVisibility(View.GONE);
            } else {
                text.setVisibility(View.VISIBLE);
                text.setText(str);
            }
        }

        public void setUser(String nickname, String deviceId, int color){
            User user = UserDao.getInstance().getUserById(deviceId);
            Drawable icon;
            if (user == null) {
                icon = TextDrawable.builder()
                        .buildRound(nickname.substring(0, 1), color);
            } else {
                icon = UserDao.getInstance().getUserById(deviceId).getAvatarDrawable();
                nickname = UserDao.getInstance().getUserById(deviceId).getName();
            }
            this.icon.setImageDrawable(icon);
            this.nickname.setText(nickname);
        }
    }

    public interface CallBack {
        void onStart();

        void onError(Throwable throwable);

        void onComplete();
    }

    public interface OnItemClickListener {
        void onLikeClick(Explore item, ExploreHolder itemView);

        void onCommentClick(Explore item, ExploreHolder itemView);

        void onItemClick(Explore item);

        void onImageClick(int pos, String[] urls, View view);
    }
}
