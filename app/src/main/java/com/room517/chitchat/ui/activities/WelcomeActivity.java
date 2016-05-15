package com.room517.chitchat.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.room517.chitchat.R;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.BaseActivity;
import com.room517.chitchat.utils.DeviceUtil;

/**
 * Created by ywwynm on 2016/5/13.
 * 引导、"注册"的Activity
 */
public class WelcomeActivity extends BaseActivity {

    private ViewPager mViewPager;
    private View[]    mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        super.init();
    }

    @Override
    protected void initMember() {

    }

    @SuppressLint("InflateParams")
    @Override
    protected void findViews() {
        mViewPager = f(R.id.vp_welcome);

        mTabs = new View[2];
        LayoutInflater inflater = LayoutInflater.from(this);
        mTabs[0] = inflater.inflate(R.layout.welcome_tab_intro, null);
        mTabs[1] = inflater.inflate(R.layout.welcome_tab_register, null);
    }

    @Override
    protected void initUI() {
        WelcomePagerAdapter adapter = new WelcomePagerAdapter();
        mViewPager.setAdapter(adapter);
    }

    @Override
    protected void setupEvents() {

    }

    private void endRegister() {
        String id = DeviceUtil.getAndroidId();
        String name = ""; // TODO: 2016/5/15 获得用户名、性别
        int sex = User.SEX_MAN;

        // TODO: 2016/5/15 获取时间、位置信息
        User user = new User(id, name, sex, "", "", 0);
        UserManager userManager = UserManager.getInstance();
        userManager.saveUserInfoToLocal(user);
        userManager.uploadUserInfoToServer(user);
    }

    private class WelcomePagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mTabs[position]);
            return mTabs[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mTabs[position]);
        }

        @Override
        public int getCount() {
            return mTabs.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
