package com.room517.chitchat.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.fragments.ImagePagerFragment;
import com.room517.chitchat.utils.DeviceUtil;

import java.util.ArrayList;

import xyz.imxqd.photochooser.model.ImageBean;

public class ImageViewerActivity extends AppCompatActivity {

    private ImagePagerFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        hideSystemStatusBar();
        initUiAndData();
    }

    private void hideSystemStatusBar() {
        if (DeviceUtil.hasJellyBeanApi()) {
//            View decorView = getWindow().getDecorView();
//            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(uiOptions);

            View decorView = getWindow().getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(flags);

            int visibility = decorView.getSystemUiVisibility();
            if (DeviceUtil.hasKitKatApi()) {
                decorView.setSystemUiVisibility(visibility
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                decorView.setSystemUiVisibility(visibility
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void initUiAndData(){
        int pos = getIntent().getIntExtra("pos", 0);
        String[] urls = getIntent().getStringArrayExtra("urls");
        ArrayList<ImageBean> list = new ArrayList<>();
        for(String url : urls) {
            ImageBean imageBean = new ImageBean(url, false);
            list.add(imageBean);
        }
        Bundle data = new Bundle();
        data.putParcelableArrayList("datas", list);
        data.putInt("position", pos);
        DisplayImageOptions options = DisplayImageOptions.createSimple();

        fragment = ImagePagerFragment.newInstance(options);
        fragment.setArguments(data);

        getFragmentManager().beginTransaction()
                .replace(R.id.image_pager, fragment)
                .commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }
}
