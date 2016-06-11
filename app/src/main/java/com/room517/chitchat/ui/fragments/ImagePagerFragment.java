package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ns.mutiphotochoser.model.ImageBean;
import com.ns.mutiphotochoser.utils.ChoseImageListener;

import java.util.ArrayList;

/**
 * Created by imxqd on 2016/6/11.
 * 朋友圈列表Fragment
 */

public class ImagePagerFragment extends Fragment{

    private ArrayList<ImageBean> mImages = null;
    private ImagePagerAdapter mAdapter = null;
    private DisplayImageOptions options = null;
    private ViewPager mImagePager = null;

    public static ImagePagerFragment newInstance(DisplayImageOptions options) {
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setImageLoader(options);
        return fragment;
    }

    private void setImageLoader(DisplayImageOptions options) {
        this.options = options;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mImagePager = new ViewPager(getActivity());
        mImages = getArguments().getParcelableArrayList("datas");
        int position = getArguments().getInt("position");
        mAdapter = new ImagePagerAdapter();
        mImagePager.setAdapter(mAdapter);
        mImagePager.setCurrentItem(position, true);
        setHasOptionsMenu(true);
        return mImagePager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(View container, int position, Object object) {
            ImageView itemView = (ImageView) object;
            ((ViewGroup) container).removeView(itemView);
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ImageBean image = mImages.get(position);
            ImageView itemView = new ImageView(getActivity());
            itemView.setScaleType(ScaleType.CENTER);
            itemView.setImageResource(com.ns.mutiphotochoser.R.drawable.default_photo);
            ImageLoader.getInstance().displayImage(image.getPath(), itemView, options);
            ((ViewGroup) container).addView(itemView);
            return itemView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (mImages == null) {
                return 0;
            } else {
                return mImages.size();
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }
}
