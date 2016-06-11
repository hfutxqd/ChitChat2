package com.room517.chitchat.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.LocationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.MainService;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.DisplayUtil;
import com.room517.chitchat.utils.JsonUtil;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by ywwynm on 2016/5/13.
 * 引导、"注册"的Activity
 */
public class WelcomeActivity extends BaseActivity {

    private ViewPager mViewPager;
    private View[]    mTabs;

    private TextView mTvAppName;
    private TextView mTvAppIntro;

    private ImageView   mIvSun;
    private int         mSunHeight;
    private ImageView[] mIvMountains;
    private int[]       mMountainHeights;

    private EditText    mEtName;
    private TextView    mTvStartAsBt;
    private ProgressBar mPbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        super.init();
    }

    @Override
    protected void initMember() {
        initMountainHeights();
    }

    private void initMountainHeights() {
        Point screen = DisplayUtil.getScreenSize();
        final int w = screen.x;

        mSunHeight = w;

        mMountainHeights = new int[3];
        mMountainHeights[0] = w * 1022 / 1440;
        mMountainHeights[1] = w * 724 / 1440;
        mMountainHeights[2] = w * 930 / 1440;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void findViews() {
        mViewPager  = f(R.id.vp_welcome);
        mTvAppName  = f(R.id.tv_app_name_welcome);
        mTvAppIntro = f(R.id.tv_app_intro_welcome);

        mTabs = new View[2];
        LayoutInflater inflater = LayoutInflater.from(this);
        mTabs[0] = inflater.inflate(R.layout.welcome_tab_intro, null);
        mTabs[1] = inflater.inflate(R.layout.welcome_tab_register, null);

        mIvSun = f(mTabs[0], R.id.iv_sun_tab_intro);

        mIvMountains = new ImageView[3];
        mIvMountains[0] = f(mTabs[0], R.id.iv_mountain_mid_tab_intro);
        mIvMountains[1] = f(mTabs[0], R.id.iv_mountain_left_tab_intro);
        mIvMountains[2] = f(mTabs[0], R.id.iv_mountain_right_tab_intro);

        mEtName      = f(mTabs[1], R.id.et_name_welcome);
        mTvStartAsBt = f(mTabs[1], R.id.tv_start_as_bt_welcome);
        mPbLoading   = f(mTabs[1], R.id.pb_loading);
    }

    @Override
    protected void initUI() {
        WelcomePagerAdapter adapter = new WelcomePagerAdapter();
        mViewPager.setAdapter(adapter);

        mTvAppIntro.setText(getResources().getStringArray(R.array.app_intro_welcome)[0]);

        resetUiElements();
        mViewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                playIntroAnimations();
            }
        }, 600);

        DisplayUtil.setSelectionHandlersColor(mEtName,
                ContextCompat.getColor(App.getApp(), R.color.app_purple));
        mPbLoading.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(App.getApp(), R.color.app_orange),
                PorterDuff.Mode.SRC_IN);
    }

    private void resetUiElements() {
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) mTvAppIntro.getLayoutParams();
        flp.topMargin = DisplayUtil.dp2px(140);
        if (DeviceUtil.hasKitKatApi()) {
            flp.topMargin += DisplayUtil.getStatusbarHeight();
        }
        mTvAppIntro.requestLayout();

        mTvAppName.setTranslationY(-DisplayUtil.dp2px(100));
        mIvSun.setTranslationY(mSunHeight);
        for (int i = 0; i < mMountainHeights.length; i++) {
            mIvMountains[i].setTranslationY(mMountainHeights[i]);
        }
    }

    @Override
    protected void setupEvents() {
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                String[] arr = getResources().getStringArray(R.array.app_intro_welcome);
                mTvAppIntro.setText(arr[position]);
            }
        });

        mTvStartAsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionCallback callback = new SimplePermissionCallback() {
                    @Override
                    public void onGranted() {
                        endRegister();
                    }
                };
                doWithPermissionChecked(callback, Def.Request.PERMISSION_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void playIntroAnimations() {
        int titleTranslationY = DisplayUtil.dp2px(60);
        if (DeviceUtil.hasKitKatApi()) {
            titleTranslationY += DisplayUtil.getStatusbarHeight();
        }
        mTvAppName.animate().setDuration(2000).translationY(titleTranslationY);

        mIvSun.animate().setDuration(2000).translationY(-mMountainHeights[0] / 2);

        final int[] durations = { 1200, 800, 1600 };
        for (int i = 0; i < mIvMountains.length; i++) {
            mIvMountains[i].animate().setDuration(durations[i]).translationY(0);
        }

        mTvAppIntro.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTvAppIntro.animate().setDuration(400).alpha(1.0f);
            }
        }, 2000);
    }

    private void updateLoadingState() {
        if (mTvStartAsBt.getVisibility() == View.VISIBLE) {
            mEtName.setInputType(InputType.TYPE_NULL);
            mTvStartAsBt.setVisibility(View.INVISIBLE);
            mTvStartAsBt.setClickable(false);
            mPbLoading.setVisibility(View.VISIBLE);
        } else {
            mEtName.setInputType(InputType.TYPE_CLASS_TEXT);
            mEtName.clearFocus();
            mTvStartAsBt.setVisibility(View.VISIBLE);
            mTvStartAsBt.setClickable(true);
            mPbLoading.setVisibility(View.INVISIBLE);
        }
    }

    private void endRegister() {
        final String name = mEtName.getText().toString();
        String validStr = User.isNameValid(name);
        if (!validStr.equals(Def.Constant.VALID)) {
            showLongToast(validStr);
            return;
        }

        final int sex = User.SEX_PRIVATE;

        updateLoadingState();

        double[] location = LocationHelper.getLocationArray();
        if (location == null) {
            showLongToast(R.string.error_cannot_get_location);
            updateLoadingState();
            return;
        }
        final double longitude = location[0];
        final double latitude  = location[1];
        Logger.i("longitude: " + longitude);
        Logger.i("latitude: "  + latitude);

        final String id = UserManager.getNewUserId();
        Logger.i("ANDROID_ID: " + id);

        Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
        MainService service = retrofit.create(MainService.class);
        RxHelper.ioMain(service.getCurrentTime(), new SimpleObserver<ResponseBody>() {

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                showLongToast(R.string.error_network_disconnected);
                updateLoadingState();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String body = responseBody.string();
                    long time = JsonUtil.getParam(body, Def.Network.TIME).getAsLong();
                    String avatar = String.valueOf(User.getRandomColorAsAvatarBackground());
                    User user = new User(id, name, sex, avatar, "",
                            longitude, latitude, time);
                    saveUserInfoAndGoToMain(user);
                } catch (IOException e) {
                    showLongToast(R.string.error_unknown);
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveUserInfoAndGoToMain(final User user) {
        final UserManager userManager = UserManager.getInstance();
        userManager.uploadUserInfoToServer(user, new SimpleObserver<ResponseBody>() {

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                showLongToast(R.string.error_network_disconnected);
                updateLoadingState();
            }

            @Override
            public void onNext(ResponseBody body) {
                try {
                    String bodyStr = body.string();
                    Logger.i(bodyStr);
                    if (Def.Network.SUCCESS.equals(
                            JsonUtil.getParam(bodyStr, Def.Network.STATUS).getAsString())) {
                        // 插入user表的原因是为了保证chat_detail表中外键约束正常
                        UserDao.getInstance().insert(user);
                        // 同时以sharedPreferences的形式保存的原因是方便识别“我”
                        userManager.saveUserInfoToLocal(user);
                        App.setMe(user);
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        showLongToast(R.string.error_unknown);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    updateLoadingState();
                }
            }
        });
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
