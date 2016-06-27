package com.room517.chitchat.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.fragments.ImagePagerFragment;

import java.util.ArrayList;

import xyz.imxqd.photochooser.model.ImageBean;

public class ImageViewerActivity extends AppCompatActivity {

    private ImagePagerFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        hideSystemStusBar();
        initUiAndData();
    }

    private void hideSystemStusBar(){
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void initUiAndData(){
        int pos = getIntent().getIntExtra("pos", 0);
        String[] urls = getIntent().getStringArrayExtra("urls");
        ArrayList<ImageBean> list = new ArrayList<>();
        for(String url : urls)
        {
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

}
