package com.room517.chitchat.ui.activities;

import android.os.Bundle;

import com.room517.chitchat.R;
import com.room517.chitchat.ui.BaseActivity;

/**
 * Created by ywwynm on 2016/5/13.
 * 打开应用后的第一个Activity，显示最近会话列表、朋友圈等
 */
public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }
}
