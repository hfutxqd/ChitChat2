package com.room517.chitchat.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import com.room517.chitchat.model.Comment;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.model.User;

import java.util.ArrayList;

import retrofit2.Retrofit;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈动态列表的适配器
 */
public class ExploreListAdapter extends RecyclerView.Adapter<ExploreListAdapter.ExploreHolder>{
    private ArrayList<Explore> mList;

    public ExploreListAdapter()
    {
        mList = new ArrayList<>();
    }

    public void set(ArrayList<Explore> list)
    {
        mList.clear();
        mList.addAll(list);
    }

    public void add(ArrayList<Explore> list)
    {
        mList.addAll(list);
    }

    public void put(ArrayList<Explore> list)
    {
        mList.addAll(list);
    }

    @Override
    public ExploreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExploreHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.explore_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ExploreHolder holder, final int position) {

        Context context = holder.nickname.getContext();
        String text = mList.get(position).getContent().getText();
        String nickname = mList.get(position).getNickname();
        String time = mList.get(position).getTime();
        String deviceId = mList.get(position).getDevice_id();
        final String[] images = mList.get(position).getContent().getImages();
        boolean isLiked = mList.get(position).isLiked();
        int like = mList.get(position).getLike();
        int comment = mList.get(position).getComment_count();
        int color = mList.get(position).getColor();

        ExploreImagesAdapter adapter = new ExploreImagesAdapter(images);
        adapter.setOnItemClickListener(new ExploreImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                mOnItemClickListener.onImageClick(pos, images);
            }
        });
        User user = UserDao.getInstance().getUserById(deviceId);
        Drawable icon;

        if(user == null)
        {
            icon =TextDrawable.builder()
                    .buildRound(nickname.substring(0, 1), color);
        }else {
            icon = UserDao.getInstance().getUserById(deviceId).getAvatarDrawable();
        }

        holder.nickname.setText(nickname);
        holder.icon.setImageDrawable(icon);
        holder.time.setText(time);
        holder.text.setText(text);
        holder.like_comment_count.setText(
                context.getString(R.string.explore_like_comment_count, like, comment));
        if(isLiked)
        {
            holder.like.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
        }else {
            holder.like.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }

        holder.images.setLayoutManager(new GridLayoutManager(holder.text.getContext(), 3));
        holder.images.setAdapter(adapter);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onLikeClick(mList.get(position), holder);
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onCommentClick(mList.get(position), holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(mList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mOnItemClickListener = listener;
    }

    public class ExploreHolder extends RecyclerView.ViewHolder{
        public ImageView icon, like, comment;
        public TextView nickname, time, text, like_comment_count;
        public RecyclerView images;

        public ExploreHolder(View itemView) {
            super(itemView);
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

    public void refresh(final CallBack callBack)
    {
        callBack.onStart();
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService exploreService = retrofit.create(ExploreService.class);
        RxHelper.ioMain(exploreService.ListExplore("0", App.getMe().getId())
                , new SimpleObserver<ArrayList<Explore>>(){
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

    public void loadMore(final CallBack callBack)
    {
        callBack.onStart();
        Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
        ExploreService exploreService = retrofit.create(ExploreService.class);
        RxHelper.ioMain(exploreService.ListExplore(mList.get(mList.size() - 1).getId(),
                App.getMe().getId()),
                new SimpleObserver<ArrayList<Explore>>(){
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

    private OnItemClickListener mOnItemClickListener = null;

    public interface CallBack{
        void onStart();
        void onError(Throwable throwable);
        void onComplete();
    }

    public interface OnItemClickListener{
        void onLikeClick(Explore item, ExploreHolder itemView);
        void onCommentClick(Explore item, ExploreHolder itemView);
        void onItemClick(Explore item);
        void onImageClick(int pos, String[] urls);
    }
}
