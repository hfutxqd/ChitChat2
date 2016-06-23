package com.room517.chitchat.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.fragments.ExploreDetailFragment;

/**
 * Created by imxqd on 2016/6/10.
 * 单条动态中图片的适配器
 */
public class ExploreImagesAdapter extends RecyclerView.Adapter<ExploreImagesAdapter.ItemViewHolder>{

    private String[] mUrls;

    public ExploreImagesAdapter(String[] urls) {
        mUrls = urls;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.explore_image_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        ImageView imageView = (ImageView) holder.itemView;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
            }
        });

        ImageLoader.getInstance().displayImage(mUrls[position], imageView);
    }

    @Override
    public int getItemCount() {
        return mUrls.length;
    }
    private OnItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }



    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int pos);
    }
}
