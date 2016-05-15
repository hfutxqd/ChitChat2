package com.room517.chitchat.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.BaseActivity;

/**
 * Created by ywwynm on 2016/5/13.
 * 打开应用后的第一个Activity，显示最近会话列表、朋友圈等
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 如果应用是安装后第一次打开，跳转到引导、"注册"页面
        SharedPreferences sp = getSharedPreferences(
                Def.Meta.PREFERENCE_NAME, MODE_PRIVATE);
        if (sp.getBoolean(Def.KEY.PREF_FIRST_LAUNCH, true)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

    }
}
