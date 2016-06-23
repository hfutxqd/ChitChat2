package com.room517.chitchat.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.ns.mutiphotochoser.model.ImageBean;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.fragments.ImagePagerFragment;

import java.util.ArrayList;


public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);
        int pos = getIntent().getIntExtra("pos", 0);
        String[] urls = getIntent().getStringArrayExtra("urls");

        DisplayImageOptions options = DisplayImageOptions.createSimple();

        ImagePagerFragment fragment =
                ImagePagerFragment.newInstance(options);


        ArrayList<ImageBean> list = new ArrayList<>();
        for(String url : urls)
        {
            ImageBean imageBean = new ImageBean(url, false);
            list.add(imageBean);
        }
        Bundle data = new Bundle();
        data.putParcelableArrayList("datas", list);
        data.putInt("position", pos);
        fragment.setArguments(data);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.image_pager, fragment)
                .commit();
    }

}
