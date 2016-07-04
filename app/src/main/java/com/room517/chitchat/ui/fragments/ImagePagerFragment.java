package com.room517.chitchat.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.dialogs.SimpleListDialog;
import com.room517.chitchat.utils.ImageCompress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import xyz.imxqd.photochooser.model.ImageBean;

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
        mImagePager = (ViewPager) root.findViewById(R.id.image_view_pager);
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

    private class ImagePagerAdapter extends PagerAdapter implements View.OnLongClickListener, PhotoViewAttacher.OnViewTapListener {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView itemView = (ImageView) object;
            container.removeView(itemView);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageBean image = mImages.get(position);
            PhotoView photoView = new PhotoView(getContext());
            photoView.setOnViewTapListener(this);
            photoView.setOnLongClickListener(this);
            photoView.setTag(image.getPath());// 分享图片时可以很容易地获取path
            ImageLoader.getInstance().displayImage(image.getPath(), photoView, options);
            container.addView(photoView);
            return photoView;
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
        public boolean onLongClick(final View view) {
            final SimpleListDialog dialog = new SimpleListDialog();
            dialog.setItems(getResources().getStringArray(R.array.image_menu));

            ArrayList<View.OnClickListener> listeners = new ArrayList<>(2);
            // 保存图片
            listeners.add(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageLoader loader = ImageLoader.getInstance();
                    Bitmap bitmap = loader.loadImageSync((String) view.getTag());
                    try {
                        ImageCompress.saveImage(bitmap);
                        Toast.makeText(getActivity(), R.string.image_saved, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), R.string.image_save_failed, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } finally {
                        dialog.dismiss();
                        bitmap.recycle();
                    }
                }
            });
            // 分享图片
            listeners.add(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageLoader loader = ImageLoader.getInstance();
                    Bitmap bitmap = loader.loadImageSync((String) view.getTag());
                    try {
                        String filename = ImageCompress.saveImageForShare(bitmap);
                        Uri uri = Uri.fromFile(new File(filename));
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(intent);
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), R.string.image_get_failed, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } finally {
                        dialog.dismiss();
                        bitmap.recycle();
                    }
                }
            });
            dialog.setOnItemClickListeners(listeners);
            dialog.show(getFragmentManager(), SimpleListDialog.class.getName());
            return true;
        }

        @Override
        public void onViewTap(View view, float v, float v1) {
            getActivity().finish();
        }
    }
}
