package com.room517.chitchat.ui.adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.room517.chitchat.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by imxqd on 2016/6/10.
 * 单条动态中图片的适配器
 */
public class ExploreImagesAdapter extends RecyclerView.Adapter<ExploreImagesAdapter.ItemViewHolder> {

    private ArrayList<String> mUrls = new ArrayList<>();
    private static final int TYPE_SINGLE = 1;
    private static final int TYPE_NORMAL = 2;
    private boolean isDetail = false;

    public ExploreImagesAdapter(String[] urls, boolean isDetail) {
        this.isDetail = isDetail;
        Collections.addAll(mUrls, urls);
    }

    public ExploreImagesAdapter(boolean isDetail) {
        this.isDetail = isDetail;
    }

    public void setUrls(String[] urls) {
        if (urls != null) {
            mUrls.clear();
            Collections.addAll(mUrls, urls);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_SINGLE){
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.explore_image_single_item, parent, false));
        }else{
            if(isDetail) {
                return new ItemViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.explore_detail_image_item, parent, false));
            } else {
                return new ItemViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.explore_image_item, parent, false));
            }
        }
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        final ImageView imageView = (ImageView) holder.itemView;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position, imageView);
            }
        });
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ImageLoader.getInstance().displayImage(mUrls.get(position), imageView);
    }

    @Override
    public int getItemCount() {
        return mUrls.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItemCount() == 1){
            return TYPE_SINGLE;
        }else {
            return TYPE_NORMAL;
        }
    }

    private OnItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public ItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, View view);
    }
}
