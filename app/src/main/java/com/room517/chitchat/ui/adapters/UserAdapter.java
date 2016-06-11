package com.room517.chitchat.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.LocationHelper;
import com.room517.chitchat.model.User;

import java.util.List;

/**
 * Created by ywwynm on 2016/5/25.
 * 用于显示附近的人
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private List<User>    mUsers;
    private List<Integer> mDistances;

    private LayoutInflater mInflater;

    public UserAdapter(Activity activity, List<User> users, List<Integer> distances) {
        mInflater = LayoutInflater.from(activity);
        mUsers = users;
        mDistances = distances;
    }

    public void setUsers(List<User> users) {
        mUsers = users;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(mInflater.inflate(R.layout.rv_user, parent, false));
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.separator.setVisibility(View.GONE);
        } else {
            holder.separator.setVisibility(View.VISIBLE);
        }

        User user = mUsers.get(position);
        holder.ivAvatar.setImageDrawable(user.getAvatarDrawable());

        String name   = user.getName();
        holder.tvName.setText(name);

        if (mDistances != null) {
            holder.tvDistance.setText(
                    LocationHelper.getDistanceDescription(mDistances.get(position)));
        }

        String tag = user.getTag();
        if (tag.isEmpty()) {
            holder.tvTag.setVisibility(View.GONE);
        } else {
            holder.tvTag.setVisibility(View.VISIBLE);
            holder.tvTag.setText(tag);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class UserHolder extends BaseViewHolder {

        ImageView ivAvatar;
        TextView  tvName;
        TextView  tvDistance;
        TextView  tvTag;
        View      separator;

        public UserHolder(View itemView) {
            super(itemView);

            ivAvatar   = f(R.id.iv_avatar_user);
            tvName     = f(R.id.tv_name_user);
            tvDistance = f(R.id.tv_distance_user);
            tvTag      = f(R.id.tv_tag_user);
            separator  = f(R.id.view_separator);

            f(R.id.rl_user).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = mUsers.get(getAdapterPosition());
                    UserDao.getInstance().insert(user);
                    RxBus.get().post(Def.Event.START_CHAT, user);
                }
            });
        }
    }

}
