package com.room517.chitchat.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import xyz.imxqd.photochooser.model.ImageBean;
import xyz.imxqd.photochooser.utils.ChoseImageListener;
import com.room517.chitchat.R;
import com.room517.chitchat.utils.DisplayUtil;

import java.util.ArrayList;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by imxqd on 2016/6/11.
 * 朋友圈列表Fragment
 */

public class ImagePagerFragment extends Fragment implements OnPageChangeListener {

    private ArrayList<ImageBean> mImages = null;
    private ImagePagerAdapter mAdapter = null;
    private DisplayImageOptions options = null;
    private ViewPager mImagePager = null;
    private TextView mPagerIndicator = null;

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
        View root = inflater.inflate(R.layout.fragment_image_pager, null);
        mImagePager = (ViewPager)root.findViewById(R.id.image_view_pager);
        mPagerIndicator = (TextView) root.findViewById(R.id.image_pager_indicator);
        mImages = getArguments().getParcelableArrayList("datas");
        int position = getArguments().getInt("position");
        mAdapter = new ImagePagerAdapter();
        mImagePager.setAdapter(mAdapter);
        mPagerIndicator.setText(getString(R.string.image_pager_indicator, position + 1, mImages.size()));
        mImagePager.setCurrentItem(position, true);
        mImagePager.addOnPageChangeListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPagerIndicator.setText(getString(R.string.image_pager_indicator, position + 1, mImages.size()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class ImagePagerAdapter extends PagerAdapter implements ImageViewTouch.OnImageViewTouchSingleTapListener {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView itemView = (ImageView) object;
            container.removeView(itemView);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageBean image = mImages.get(position);
            ImageViewTouch itemView = new ImageViewTouch(getContext());
            itemView.setSingleTapListener(this);
            itemView.setScaleType(ScaleType.MATRIX);
            ImageLoader.getInstance().displayImage(image.getPath(), itemView, options);
            container.addView(itemView);
            return itemView;
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

        @Override
        public void onSingleTapConfirmed() {
            getActivity().finish();
        }
    }
}
