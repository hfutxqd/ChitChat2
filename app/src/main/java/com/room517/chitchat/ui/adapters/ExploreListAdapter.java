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
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.User;

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
            return;
        }
        final int pos = postion - 1;
//        System.out.println("id---->" + mList.get(pos).getId());
        Context context = holder.nickname.getContext();
        String text = mList.get(pos).getContent().getText();
        String nickname = mList.get(pos).getNickname();
        String time = mList.get(pos).getTime();
        String deviceId = mList.get(pos).getDevice_id();
        final String[] images = mList.get(pos).getContent().getImages();
        boolean isLiked = mList.get(pos).isLiked();
        int like = mList.get(pos).getLike();
        int comment = mList.get(pos).getComment_count();
        int color = mList.get(pos).getColor();
        ExploreImagesAdapter adapter = new ExploreImagesAdapter(images);
        adapter.setOnItemClickListener(new ExploreImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                mOnItemClickListener.onImageClick(pos, images);
            }
        });
        User user = UserDao.getInstance().getUserById(deviceId);
        Drawable icon;

        if (user == null) {
            icon = TextDrawable.builder()
                    .buildRound(nickname.substring(0, 1), color);
        } else {
            icon = UserDao.getInstance().getUserById(deviceId).getAvatarDrawable();
        }

        holder.nickname.setText(nickname);
        holder.icon.setImageDrawable(icon);
        holder.time.setText(time);
        if (text.trim().length() == 0) {
            holder.text.setVisibility(View.GONE);
        } else {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(text);
        }

        holder.like_comment_count.setText(
                context.getString(R.string.explore_like_comment_count, like, comment));
        if (isLiked) {
            holder.like.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
        } else {
            holder.like.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }
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

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onLikeClick(mList.get(pos), holder);
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onCommentClick(mList.get(pos), holder);
            }
        });

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
        public ImageView icon, like, comment;
        public TextView nickname, time, text, like_comment_count;
        public RecyclerView images;
        public int viewType;

        public ExploreHolder(View itemView, int viewType){
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
            like_comment_count = (TextView) itemView.findViewById(R.id.explore_like_comment_count);
            images = (RecyclerView) itemView.findViewById(R.id.explore_item_images);
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

        void onImageClick(int pos, String[] urls);
    }
}
