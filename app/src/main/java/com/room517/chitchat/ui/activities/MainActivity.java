package com.room517.chitchat.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
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
import com.room517.chitchat.helpers.AMapLocationHelper;
import com.room517.chitchat.helpers.NotificationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RongHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.MainService;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.Chat;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.simpleinterface.SimpleTextWatcher;
import com.room517.chitchat.ui.dialogs.ThreeActionsDialog;
import com.room517.chitchat.ui.fragments.ChatListFragment;
import com.room517.chitchat.ui.fragments.ExploreListFragment;
import com.room517.chitchat.ui.fragments.NearbyPeopleFragment;
import com.room517.chitchat.ui.fragments.SearchFragment;
import com.room517.chitchat.ui.views.FloatingActionButton;
import com.room517.chitchat.ui.views.reveal.RevealLayout;
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

    // TODO: 2016/7/12 融云有些问题

    private Toolbar mActionBar;
    private FloatingActionButton mFab;

    private RevealLayout mRevealLayout;
    private ImageView mIvBackSearch;
    private EditText mEtSearch;
    private final int SEARCH_ANIM_DURATION = 360;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ChatListFragment mChatListFragment;
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
                toggleSearchUi();
                return true;
            case R.id.act_check_me_detail:
                Intent intent = new Intent(this, UserActivity.class);
                intent.putExtra(Def.Key.USER, App.getMe());
                startActivity(intent);
                return true;
            case R.id.act_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.act_about:
                // // TODO: 2016/7/13 关于信息
                return true;
            case R.id.act_license:
                startActivity(new Intent(this, LicenseActivity.class));
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

    private void toggleSearchUi() {
        View v = f(R.id.act_search);
        int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        int x = pos[0] + v.getWidth() / 2;
        int y = v.getTop() + v.getHeight() / 2;

        final String tag = SearchFragment.class.getName();

        if (mRevealLayout.getVisibility() == View.VISIBLE) {
            KeyboardUtil.hideKeyboard(getCurrentFocus());
            mRevealLayout.hide(x, y, SEARCH_ANIM_DURATION);
            mRevealLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEtSearch.setText("");
                    mRevealLayout.setVisibility(View.INVISIBLE);
                }
            }, SEARCH_ANIM_DURATION);

            mFab.spread();
            getSupportFragmentManager().popBackStack();
        } else {
            mRevealLayout.setVisibility(View.VISIBLE);
            mRevealLayout.show(x, y, SEARCH_ANIM_DURATION);
            mRevealLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    KeyboardUtil.showKeyboard(mEtSearch);
                }
            }, SEARCH_ANIM_DURATION);


            mFab.shrink();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_main, new SearchFragment(), tag)
                    .addToBackStack(tag)
                    .commit();
        }
    }

    private void exit() {
        View.OnClickListener firstListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIMClient.getInstance().disconnect();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = App.getMe();
                AMapLocation location = App.getLocationHelper().getLocationSync();
                if (location != null) {
                    user.setLatitude(location.getLatitude());
                    user.setLongitude(location.getLongitude());
                }
                UserManager.getInstance().saveUserInfoToLocal(user);
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
        }).start();
    }

    private void prepareConnectRongServer() {
        User me = App.getMe();
        String userId = me.getId();
        String name = me.getName();
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
                            String body = responseBody.string();
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
                if (fromId.equals(Def.Constant.COMMENT_SYSTEM_ID)) {
                    return receiveComment(message);
                } else {
                    return receiveChatDetailMessage(message);
                }
            }
        });
    }

    private boolean receiveComment(Message message) {
        String json = ((TextMessage) message.getContent()).getContent();
        final String exploreId = JsonUtil.getParam(json, "explore_id").getAsString();
        final String userId = JsonUtil.getParam(json, "user_id").getAsString();
        final String color = JsonUtil.getParam(json, "color").getAsString();
        final String nickname = JsonUtil.getParam(json, "nickname").getAsString();
        final String content = JsonUtil.getParam(json, "content").getAsString();
        User user = UserDao.getInstance().getUserById(userId);
        if (user == null) {
            User tmp = new User(userId, nickname, User.SEX_PRIVATE, color, "", 0, 0, 0);
            UserDao.getInstance().insert(tmp);
        }
        NotificationHelper.notifyComment(App.getApp(), exploreId, userId, content);
        return true;
    }

    private boolean receiveChatDetailMessage(Message message) {
        final ChatDetail chatDetail = new ChatDetail(message);
        final UserDao userDao = UserDao.getInstance();
        String fromId = message.getSenderUserId();
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
                    receiveChatDetail(chatDetail);
                }
            });
        } else {
            receiveChatDetail(chatDetail);
        }
        return true;
    }

    private void receiveChatDetail(ChatDetail chatDetail) {
        if (chatDetail.isCmd()) {
            receiveCmd(chatDetail);
        } else { // receive text message, future there will be image message, voice message and so on.
            String fromId = chatDetail.getFromId();
            ChatDao chatDao = ChatDao.getInstance();
            if (chatDao.getChat(fromId, false) == null) {
                Chat chat = new Chat(fromId, Chat.TYPE_NORMAL);
                chatDao.insertChat(chat);
            }
            chatDao.insertChatDetail(chatDetail);

            if (App.shouldNotifyMessage(fromId)) {
                @ChatDetail.Type int type = chatDetail.getType();
                String notificationContent = "";
                if (type == ChatDetail.TYPE_TEXT) {
                    notificationContent = chatDetail.getContent();
                } else if (type == ChatDetail.TYPE_IMAGE) {
                    notificationContent = getString(R.string.middle_bracket_image);
                } else if (type == ChatDetail.TYPE_AUDIO) {
                    notificationContent = getString(R.string.middle_bracket_audio);
                } else if (type == ChatDetail.TYPE_LOCATION) {
                    AMapLocation location = AMapLocationHelper.getLocationFromString(
                            chatDetail.getContent());
                    if (location != null) {
                        notificationContent = location.getPoiName();
                    } else {
                        throw new IllegalStateException(
                                "Received ChatDetail with type of Location but location is null.");
                    }
                }
                NotificationHelper.notifyMessage(this, fromId, notificationContent);
            }

            RxBus.get().post(Def.Event.ON_RECEIVE_MESSAGE, chatDetail);
        }
    }

    private void receiveCmd(ChatDetail cmd) {
        @ChatDetail.Type int type = cmd.getType();
        if (type == ChatDetail.TYPE_CMD_WITHDRAW) {
            receiveWithdraw(cmd);
        } else if (type == ChatDetail.TYPE_CMD_WITHDRAW_RESULT) {
            ChatDao chatDao = ChatDao.getInstance();
            String[] content = cmd.getContent().split(",");
            String withdrawId = content[0];
            String result = content[1];

            ChatDetail withdraw = chatDao.getChatDetail(withdrawId);
            String toWithdrawId = withdraw.getContent();
            ChatDetail toWithdraw = chatDao.getChatDetail(toWithdrawId);

            if (Def.Constant.SUCCESS.equals(result)) { // 撤回成功
                chatDao.deleteChatDetail(toWithdrawId);
                if (toWithdraw == null) {
                    toWithdraw = ChatDetail.newTempChatDetail(
                            toWithdrawId, App.getMe().getId(), withdraw.getToId());
                }
                RxBus.get().post(Def.Event.ON_DELETE_MESSAGE, toWithdraw);
            } else if (toWithdraw != null) { // 撤回失败
                chatDao.updateChatDetailState(toWithdrawId, ChatDetail.STATE_WITHDRAW_FAILED);
                toWithdraw.setState(ChatDetail.STATE_WITHDRAW_FAILED);
                RxBus.get().post(Def.Event.UPDATE_MESSAGE_STATE, toWithdraw);
            }
        }
    }

    private void receiveWithdraw(ChatDetail withdraw) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean canWithdraw = sp.getBoolean(Def.Key.PrefSettings.CAN_WITHDRAW, true);
        if (canWithdraw) {
            ChatDao chatDao = ChatDao.getInstance();
            String toWithdrawId = withdraw.getContent();
            ChatDetail toWithdraw = chatDao.getChatDetail(toWithdrawId);
            chatDao.deleteChatDetail(toWithdrawId);

            if (toWithdraw == null) {
                toWithdraw = ChatDetail.newTempChatDetail(
                        toWithdrawId, withdraw.getFromId(), App.getMe().getId());
            }
            RxBus.get().post(Def.Event.ON_DELETE_MESSAGE, toWithdraw);
        }

        String fromId = App.getMe().getId();
        String id = ChatDetail.newChatDetailId(fromId);
        String toId = withdraw.getFromId();
        int type = ChatDetail.TYPE_CMD_WITHDRAW_RESULT;
        int state = ChatDetail.STATE_SENDING;

        String content = withdraw.getId() + ",";
        if (canWithdraw) {
            content += Def.Constant.SUCCESS;
        } else {
            content += Def.Constant.FAILED;
        }

        long time = System.currentTimeMillis();

        ChatDetail result = new ChatDetail(id, fromId, toId, type, state, content, time);
        sendResultChatDetail(result);
    }

    private void sendResultChatDetail(final ChatDetail result) {
        RongHelper.sendCmdMessage(result, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                ChatDao.getInstance().updateChatDetailState(
                        result.getId(), ChatDetail.STATE_SEND_FAILED);
            }

            @Override
            public void onSuccess(Integer integer) {
                ChatDao.getInstance().updateChatDetailState(
                        result.getId(), ChatDetail.STATE_NORMAL);
            }
        });
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
        mChatListFragment = ChatListFragment.newInstance(null);
        mExploreListFragment = ExploreListFragment.newInstance(null);
    }

    @Override
    protected void findViews() {
        mActionBar = f(R.id.actionbar);
        mFab       = f(R.id.fab_main);

        mRevealLayout = f(R.id.reveal_layout_search);
        mIvBackSearch = f(R.id.iv_back_search_as_bt);
        mEtSearch     = f(R.id.et_search);

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
        setupActionBarEvents();
        setupSearchEvents();
        setupFabEvent();
        setupViewPagerEvents();
    }

    private void setupActionBarEvents() {
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
    }

    private void setupSearchEvents() {
        mIvBackSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchUi();
            }
        });
        mEtSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                RxBus.get().post(Def.Event.SEARCH, s.toString());
            }
        });
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtil.hideKeyboard(getCurrentFocus());
                    return true;
                }
                return false;
            }
        });
    }

    private void setupFabEvent() {
        final String tag = NearbyPeopleFragment.class.getName();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewPager.getCurrentItem() == 0) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container_main, NearbyPeopleFragment.newInstance(null), tag)
                            .addToBackStack(tag)
                            .commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    Intent intent = new Intent(MainActivity.this, PublishActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void setupViewPagerEvents() {
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mFab.showFromBottom();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mRevealLayout.getVisibility() == View.VISIBLE) {
            toggleSearchUi();
        } else {
            super.onBackPressed();
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







    // ----------------------------- Event Subscribers ----------------------------- //


    @Subscribe(tags = {@Tag(Def.Event.SHOW_FAB_FROM_BOTTOM)})
    public void showFabFromBottom(Object event) {
        mFab.showFromBottom();
    }

    @Subscribe(tags = {@Tag(Def.Event.HIDE_FAB_TO_BOTTOM)})
    public void hideToBottom(Object event) {
        mFab.hideToBottom();
    }

    @Subscribe(tags = {@Tag(Def.Event.PREPARE_FOR_FRAGMENT)})
    public void prepareForFragments(Object event) {
        mTabLayout.setVisibility(View.GONE);
        mFab.shrink();
    }

    @Subscribe(tags = {@Tag(Def.Event.BACK_FROM_FRAGMENT)})
    public void backFromFragment(Object event) {
        System.out.println(getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            setActionBarAppearance();
            KeyboardUtil.hideKeyboard(getCurrentFocus());
            mTabLayout.setVisibility(View.VISIBLE);
            mFab.spread();
        }
    }

    @Subscribe(tags = {@Tag(Def.Event.START_CHAT)})
    public void startChat(Def.Event.StartChat startChat) {
        Intent intent = new Intent(this, ChatDetailsActivity.class);
        intent.putExtra(Def.Key.USER, startChat.user);
        intent.putExtra(Def.Key.CHAT_DETAIL, startChat.chatDetailToForward);
        intent.putExtra(Def.Key.CHAT_DETAIL_SCROLL, startChat.chatDetailToScroll);
        startActivity(intent);
    }
}
