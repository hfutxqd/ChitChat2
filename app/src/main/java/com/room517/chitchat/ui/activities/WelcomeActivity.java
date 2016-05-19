package com.room517.chitchat.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.helper.LocationHelper;
import com.room517.chitchat.helper.RetrofitHelper;
import com.room517.chitchat.helper.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.SimpleTimeService;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.SimpleTime;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.BaseActivity;
import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.DisplayUtil;

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

    private ImageView mIvSun;
    private int mSunHeight;
    private ImageView[] mIvMountains;
    private int[] mMountainHeights;

    private EditText mEtName;

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

        mEtName = f(mTabs[1], R.id.et_name_welcome);
    }

    @Override
    protected void initUI() {
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

        WelcomePagerAdapter adapter = new WelcomePagerAdapter();
        mViewPager.setAdapter(adapter);
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

    public void onStartClicked(View view) {
        saveUserInfoAndGoToMain(new User("1", "ywwynm", 1, "", 0, 0, 0));
//        PermissionCallback callback = new SimplePermissionCallback() {
//            @Override
//            public void onGranted() {
//                endRegister();
//            }
//        };
//        doWithPermissionChecked(callback, Def.Request.PERMISSION_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void endRegister() {
        final String name = mEtName.getText().toString();
        final int nameLen = name.length();
        if (nameLen < 1 || nameLen > 20) {
            showLongToast(R.string.error_name_incorrect);
            return;
        }

        final int sex = User.SEX_PRIVATE;

        double[] location = LocationHelper.getLocationArray();
        if (location == null) {
            showLongToast(R.string.error_cannot_get_location);
            return;
        }
        final double longitude = location[0];
        final double latitude  = location[1];
        mLogger.i("longitude: " + longitude);
        mLogger.i("latitude: "  + latitude);

        final String id = UserManager.getNewUserId();
        mLogger.i("ANDROID_ID: " + id);

        Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
        SimpleTimeService service = retrofit.create(SimpleTimeService.class);
        RxHelper.ioMain(service.getCurrentTime(), new SimpleObserver<SimpleTime>() {

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                showLongToast(R.string.error_network_disconnected);
            }

            @Override
            public void onNext(SimpleTime simpleTime) {
                User user = new User(id, name, sex, "", longitude, latitude, simpleTime.getTime());
                saveUserInfoAndGoToMain(user);
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
            }

            @Override
            public void onNext(ResponseBody body) {
                try {
                    mLogger.i(body.contentLength());
                    mLogger.i(body.contentType());
                    String bodyStr = body.string();
                    mLogger.i(bodyStr);
                    if (Def.Network.SUCCESS.equals(bodyStr)) {
//                        userManager.saveUserInfoToLocal(user);
//                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
//                        startActivity(intent);
//                        finish();
                    } else {
                        showLongToast(R.string.error_network_disconnected);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
