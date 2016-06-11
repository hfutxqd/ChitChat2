package com.room517.chitchat.ui.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.model.Comment;
import com.room517.chitchat.model.User;

import java.util.ArrayList;

/**
 * Created by imxqd on 2016/6/11.
 * 评论适配器
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder>{

    ArrayList<Comment> mList;

    public CommentAdapter()
    {
        mList = new ArrayList<>();
    }

    public void set(ArrayList<Comment> list)
    {
        mList.clear();
        mList.addAll(list);
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.detail_comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        String nickname = mList.get(position).getNickname();
        String text = mList.get(position).getText();
        String time = mList.get(position).getTime();
        String deviceId = mList.get(position).getDevice_id();

        int color = mList.get(position).getColor();

        holder.nickname.setText(nickname);
        holder.text.setText(text);
        holder.time.setText(time);

        User user = UserDao.getInstance().getUserById(deviceId);
        Drawable icon;

        if(user == null)
        {
            icon = TextDrawable.builder()
                    .buildRound(nickname.substring(0, 1), color);
        }else {
            icon = UserDao.getInstance().getUserById(deviceId).getAvatarDrawable();
        }

        holder.icon.setImageDrawable(icon);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class CommentHolder extends RecyclerView.ViewHolder{
        public ImageView icon;
        public TextView nickname, text, time;

        public CommentHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.comment_item_icon);
            nickname = (TextView) itemView.findViewById(R.id.comment_item_nickname);
            text = (TextView) itemView.findViewById(R.id.comment_item_text);
            time = (TextView) itemView.findViewById(R.id.comment_item_time);
        }
    }
}
