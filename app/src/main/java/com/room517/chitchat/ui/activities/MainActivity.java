package com.room517.chitchat.ui.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.fragments.ChatDetailFragment;
import com.room517.chitchat.ui.fragments.ChatListFragment;
import com.room517.chitchat.ui.fragments.ExploreListFragment;
import com.room517.chitchat.ui.fragments.NearbyPeopleFragment;
import com.room517.chitchat.ui.views.FloatingActionButton;

/**
 * Created by ywwynm on 2016/5/13.
 * 打开应用后的第一个Activity，显示最近会话列表、朋友圈等
 */
public class MainActivity extends BaseActivity {

    private Toolbar              mActionBar;
    private FloatingActionButton mFab;

    private ViewPager           mViewPager;
    private TabLayout           mTabLayout;
    private ChatListFragment    mChatListFragment;
    private ExploreListFragment mExploreListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxBus.get().register(this);

        // 如果应用是安装后第一次打开，跳转到引导、"注册"页面
        // TODO: 2016/5/24 在正式版本中加上这些代码
//        SharedPreferences sp = getSharedPreferences(
//                Def.Meta.PREFERENCE_USER_ME, MODE_PRIVATE);
//        if (!sp.contains(Def.Key.PrefUserMe.ID)) {
//            Intent intent = new Intent(this, WelcomeActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }

        setContentView(R.layout.activity_main);

        super.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initMember() {
        mChatListFragment    = ChatListFragment.newInstance(null);
        mExploreListFragment = ExploreListFragment.newInstance(null);
    }

    @Override
    protected void findViews() {
        mActionBar = f(R.id.actionbar);
        mFab       = f(R.id.fab_main);

        mViewPager = f(R.id.vp_main);
        mTabLayout = f(R.id.tab_layout);
    }

    @Override
    protected void initUI() {
        initActionBar();

        mFab.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_orange)));
        mFab.setRippleColor(ContextCompat.getColor(this, R.color.fab_ripple_white));

        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);
        TabLayout.Tab tab0 = mTabLayout.getTabAt(0);
        if (tab0 != null) {
            tab0.setIcon(R.drawable.ic_chat);
        }
        TabLayout.Tab tab1 = mTabLayout.getTabAt(1);
        if (tab1 != null) {
            tab1.setIcon(R.drawable.ic_explore);
        }
    }

    private void initActionBar() {
        setSupportActionBar(mActionBar);
        setActionBarAppearance();
    }

    private void setActionBarAppearance() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(null);
        }
    }

    @Override
    protected void setupEvents() {
        setupFabEvent();
    }

    private void setupFabEvent() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareForFragments();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container_main, NearbyPeopleFragment.newInstance(null))
                        .addToBackStack(NearbyPeopleFragment.class.getName())
                        .commit();
            }
        });
    }

    public void prepareForFragments() {
        mTabLayout.setVisibility(View.GONE);
        mFab.shrink();
    }

    @Subscribe(tags = { @Tag(Def.Event.BACK_FROM_FRAGMENT) })
    public void backFromFragment(Object event) {
        setActionBarAppearance();
        mTabLayout.setVisibility(View.VISIBLE);
        mFab.spread();
    }

    @Subscribe(tags = { @Tag(Def.Event.START_CHAT) })
    public void startChat(User user) {
        prepareForFragments();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();

        Bundle args = new Bundle();
        args.putParcelable(Def.Key.USER, user);
        fragmentManager
                .beginTransaction()
                .add(R.id.container_main, ChatDetailFragment.newInstance(args))
                .addToBackStack(ChatDetailFragment.class.getName())
                .commit();
    }

    public Toolbar getToolbar() {
        return mActionBar;
    }

    class MainFragmentPagerAdapter extends FragmentPagerAdapter {

        public MainFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mChatListFragment;
            } else if (position == 1) {
                return mExploreListFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
