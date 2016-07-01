package com.room517.chitchat.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.BuildConfig;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.ChatDao;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.LocationHelper;
import com.room517.chitchat.helpers.NotificationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.MainService;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.dialogs.ThreeActionsDialog;
import com.room517.chitchat.ui.fragments.ChatDetailsFragment;
import com.room517.chitchat.ui.fragments.ChatListFragment;
import com.room517.chitchat.ui.fragments.ExploreListFragment;
import com.room517.chitchat.ui.fragments.NearbyPeopleFragment;
import com.room517.chitchat.ui.views.FloatingActionButton;
import com.room517.chitchat.utils.JsonUtil;
import com.room517.chitchat.utils.KeyboardUtil;

import java.io.IOException;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import xyz.imxqd.licenseview.LicenseView;

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
        SharedPreferences sp = getSharedPreferences(
                Def.Meta.PREFERENCE_USER_ME, MODE_PRIVATE);
        if (!sp.contains(Def.Key.PrefUserMe.ID)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // TODO: 2016/6/7 network error when connecting to our/rong server
        connectToOurServer();
        prepareConnectRongServer();

        super.init();

        startChatClickingNotification(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.i("MainActivity onNewIntent");
        startChatClickingNotification(intent);
    }

    private void startChatClickingNotification(Intent intent) {
        if (intent == null) {
            return;
        }

        User user = intent.getParcelableExtra(Def.Key.USER);
        if (user != null) {
            startChat(user);
            intent.removeExtra(Def.Key.USER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.act_search:
                search();
                return true;
            case R.id.act_check_me_detail:
                Intent intent = new Intent(this, UserActivity.class);
                intent.putExtra(Def.Key.USER, App.getMe());
                startActivity(intent);
                return true;
            case R.id.act_about:
                LicenseView licenseView = new LicenseView(this);
                licenseView.setLicenses(R.xml.licenses);
                new AlertDialog.Builder(this)
                        .setView(licenseView)
                        .setPositiveButton(R.string.act_confirm, null)
                        .show();
                return true;
            case R.id.act_exit:
                exit();
                return true;
            case R.id.act_check_user_detail:
                RxBus.get().post(
                        Def.Event.CHECK_USER_DETAIL, f(mActionBar, R.id.act_check_user_detail));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void search() {

    }

    private void exit() {
        View.OnClickListener firstListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        View.OnClickListener secondListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManager.getInstance().logoutFromServer();
                RongIMClient.getInstance().logout();
                finish();
            }
        };

        ThreeActionsDialog tad =
                new ThreeActionsDialog.Builder(Def.Meta.APP_PURPLE)
                        .title(getString(R.string.alert_exit_chitchat_title))
                        .content(getString(R.string.alert_exit_chitchat_content))
                        .firstActionText(getString(R.string.alert_exit_still_new_message))
                        .firstActionListener(firstListener)
                        .secondActionText(getString(R.string.alert_exit_no_new_message))
                        .secondActionListener(secondListener)
                        .cancelText(getString(R.string.act_cancel))
                        .build();
        tad.show(getFragmentManager(), ThreeActionsDialog.class.getName());
    }

    private void connectToOurServer() {
        User user = App.getMe();
        double[] locations = LocationHelper.getLocationArray();
        if (locations != null) {
            user.setLongitude(locations[0]);
            user.setLatitude(locations[1]);
        }
        UserManager.getInstance().uploadUserInfoToServer(user,
                new SimpleObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody body) {
                        try {
                            String bodyStr = body.string();
                            if (BuildConfig.DEBUG) {
                                Logger.i("connect to our server: " + bodyStr);
                            }

                            if (!Def.Network.SUCCESS.equals(
                                    JsonUtil.getParam(bodyStr, Def.Network.STATUS).getAsString())) {
                                showLongToast(R.string.error_unknown);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void prepareConnectRongServer() {
        User   me     = App.getMe();
        String userId = me.getId();
        String name   = me.getName();
        String avatar = me.getAvatar();

        Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
        MainService service = retrofit.create(MainService.class);
        RxHelper.ioMain(service.getRongToken(userId, name, avatar),
                new SimpleObserver<ResponseBody>() {

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        showLongToast(R.string.error_network_disconnected);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String body  = responseBody.string();
                            Logger.json(body);

                            String token = JsonUtil.getParam(body, Def.Network.TOKEN).getAsString();
                            setupRongListeners();
                            connectRongServer(token);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showLongToast(R.string.error_unknown);
                        }
                    }
                });
    }

    private void setupRongListeners() {
        RongIMClient.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int leftCount) {
                String fromId = message.getSenderUserId();
                String text = ((TextMessage) message.getContent()).getContent();
                System.out.println(fromId);
                System.out.println(text);
                if(fromId.equals(Def.Constant.SYSTEM_ID)){
                    String json = ((TextMessage) message.getContent()).getContent();
                    final String exploreId = JsonUtil.getParam(json, "explore_id").getAsString();
                    final String userId = JsonUtil.getParam(json, "user_id").getAsString();
                    final String color = JsonUtil.getParam(json, "color").getAsString();
                    final String nickname = JsonUtil.getParam(json, "nickname").getAsString();
                    final String content = JsonUtil.getParam(json, "content").getAsString();
                    User user = UserDao.getInstance().getUserById(userId);
                    if(user == null){
                        User tmp = new User(userId, nickname, User.SEX_PRIVATE, color, "", 0, 0, 0);
                        UserDao.getInstance().insert(tmp);
                    }
                    NotificationHelper.notifyComment(App.getApp(), exploreId, userId, content);
                    return true;
                }
                final ChatDetail chatDetail = new ChatDetail(message);
                final UserDao userDao = UserDao.getInstance();
                if (userDao.getUserById(fromId) == null) {
                    /*
                        数据库中还没有该User，插入新的Chat或ChatDetail都会失败（因为外键的缘故），
                        所以需要先从服务器获取该User的完整信息并插入到本地数据库
                     */
                    Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
                    UserService service = retrofit.create(UserService.class);
                    RxHelper.ioMain(service.getUserById(fromId), new SimpleObserver<User>() {
                        @Override
                        public void onError(Throwable throwable) {
                            Logger.e(throwable.getMessage());
                        }

                        @Override
                        public void onNext(User user) {
                            userDao.insert(user);
                            insertChatDetailAndPostEvent(chatDetail);
                        }
                    });
                } else {
                    insertChatDetailAndPostEvent(chatDetail);
                }
                return true;
            }
        });
    }

    private void insertChatDetailAndPostEvent(ChatDetail chatDetail) {
        String fromId = chatDetail.getFromId();
        ChatDao chatDao = ChatDao.getInstance();
        if (chatDao.getChat(fromId, false) == null) {
            Chat chat = new Chat(fromId, Chat.TYPE_NORMAL);
            chatDao.insertChat(chat);
        }
        chatDao.insertChatDetail(chatDetail);

        if (App.shouldNotifyMessage(fromId)) {
            NotificationHelper.notifyMessage(this, fromId, chatDetail.getContent());
        }

        RxBus.get().post(Def.Event.ON_RECEIVE_MESSAGE, chatDetail);
    }

    private void connectRongServer(String token) {
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                prepareConnectRongServer();
            }

            @Override
            public void onSuccess(String s) {
                if (BuildConfig.DEBUG) {
                    Logger.i("Connect Rong successfully!\ntoken: " + s);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (BuildConfig.DEBUG) {
                    Logger.e("Connect Rong failed\nerror: " + errorCode.getMessage());
                }
            }
        });
    }

    public FloatingActionButton getFab() {
        return mFab;
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
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStack();
            }
        });
        mActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.get().post(Def.Event.ON_ACTIONBAR_CLICKED, mViewPager.getCurrentItem());
            }
        });
        setupFabEvent();
    }

    private void setupFabEvent() {
        final String tag = NearbyPeopleFragment.class.getName();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewPager.getCurrentItem() == 0)
                {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container_main, NearbyPeopleFragment.newInstance(null), tag)
                            .addToBackStack(tag)
                            .commit();
                    getSupportFragmentManager().executePendingTransactions();
                }else {
                    Intent intent = new Intent(MainActivity.this, PublishActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Subscribe(tags = { @Tag(Def.Event.PREPARE_FOR_FRAGMENT) })
    public void prepareForFragments(Object event) {
        mTabLayout.setVisibility(View.GONE);
        mFab.shrink();
    }

    @Subscribe(tags = { @Tag(Def.Event.BACK_FROM_FRAGMENT) })
    public void backFromFragment(Object event) {
        setActionBarAppearance();
        KeyboardUtil.hideKeyboard(getCurrentFocus());
        mTabLayout.setVisibility(View.VISIBLE);
        mFab.spread();
    }

    @Subscribe(tags = { @Tag(Def.Event.START_CHAT) })
    public void startChat(User user) {
        shouldNotBackFromFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();

        final String tag = ChatDetailsFragment.class.getName();
        Bundle args = new Bundle();
        args.putParcelable(Def.Key.USER, user);
        fragmentManager
                .beginTransaction()
                .replace(R.id.container_main, ChatDetailsFragment.newInstance(args), tag)
                .addToBackStack(tag)
                .commit();
        fragmentManager.executePendingTransactions();
    }

    private void shouldNotBackFromFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        NearbyPeopleFragment npf = (NearbyPeopleFragment) fragmentManager.findFragmentByTag(
                NearbyPeopleFragment.class.getName());
        if (npf != null) {
            npf.setShouldBackFromFragment(false);
        }

        ChatDetailsFragment cdf = (ChatDetailsFragment) fragmentManager.findFragmentByTag(
                ChatDetailsFragment.class.getName());
        if (cdf != null) {
            cdf.setShouldBackFromFragment(false);
        }
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
