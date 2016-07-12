package com.room517.chitchat.ui.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.manager.UserManager;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.dialogs.ColorChooserDialog;
import com.room517.chitchat.ui.dialogs.InputDialog;
import com.room517.chitchat.ui.dialogs.ListChooserDialog;
import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.DisplayUtil;
import com.room517.chitchat.utils.JsonUtil;
import com.room517.chitchat.utils.KeyboardUtil;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class UserActivity extends BaseActivity {

    // TODO: 2016/7/12 无网络不能返回

    private User    mUser;
    private boolean mEditable;

    private String[] mSexStrs;

    private Toolbar mActionbar;

    private ImageView mIvAvatar;
    private TextView  mTvName;
    private TextView  mTvTag;
    private TextView  mTvSex;

    private EditText mEtHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        RxBus.get().register(this);
        super.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Override
    protected void initMember() {
        User user = getIntent().getParcelableExtra(Def.Key.USER);
        mUser = new User(user);

        mEditable = mUser.getId().equals(App.getMe().getId());

        mSexStrs = getResources().getStringArray(R.array.sex);
    }

    @Override
    protected void findViews() {
        mActionbar = f(R.id.actionbar);

        mIvAvatar = f(R.id.iv_avatar_user);
        mTvName   = f(R.id.tv_name_user);
        mTvTag    = f(R.id.tv_tag_user);
        mTvSex    = f(R.id.tv_sex_user);

        mEtHidden = f(R.id.et_hidden);
    }

    @Override
    protected void initUI() {
        initActionbar();
        setTopColor(mUser.getColor());

        mIvAvatar.setImageDrawable(mUser.getAvatarDrawable());
        mTvName.setText(mUser.getName());
        mTvTag.setText(mUser.getTag());
        mTvSex.setText(mSexStrs[mUser.getSex()]);
    }

    private void initActionbar() {
        setSupportActionBar(mActionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mUser.getName());
        }
    }

    private void setTopColor(int color) {
        if (DeviceUtil.hasLollipopApi()) {
            getWindow().setStatusBarColor(DisplayUtil.getDarkColor(color));
        }
        mActionbar.setBackgroundColor(color);
    }

    @Override
    protected void setupEvents() {
        mActionbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Subscribe(tags = { @Tag(Def.Event.BACK_FROM_DIALOG) })
    public void backFromDialog(Object event) {
        mEtHidden.requestFocus();
        KeyboardUtil.hideKeyboard(mEtHidden);
    }

    public void onRlAvatarClicked(View view) {
        if (!mEditable) return;
        ColorChooserDialog ccd = new ColorChooserDialog.Builder(mUser.getColor())
                .title(getString(R.string.change_avatar))
                .initialColor(mUser.getColor())
                .callback(new ColorChooserDialog.Callback() {
                    @Override
                    public void onColorPicked(int color) {
                        setTopColor(color);
                        mUser.setAvatar(String.valueOf(color));
                        mIvAvatar.setImageDrawable(mUser.getAvatarDrawable());
                    }
                })
                .build();
        ccd.show(getFragmentManager(), ColorChooserDialog.class.getName());
    }

    public void onRlNameClicked(View view) {
        if (!mEditable) return;
        final InputDialog id = new InputDialog.Builder(mUser.getColor())
                .title(getString(R.string.change_name))
                .initialText(mUser.getName())
                .hint(getString(R.string.hint_new_name))
                .maxLength(20)
                .cancelText(getString(R.string.act_cancel))
                .confirmText(getString(R.string.act_confirm))
                .build();
        id.setCallback(new InputDialog.Callback() {
            @Override
            public void onConfirm(String name) {
                String valid = User.isNameValid(name);
                if (Def.Constant.VALID.equals(valid)) {
                    id.dismiss();
                    mTvName.setText(name);
                    mUser.setName(name);
                } else {
                    id.setError(valid);
                }
            }

            @Override
            public void onCancel() {

            }
        });
        id.show(getFragmentManager(), InputDialog.class.getName());
    }

    public void onRlTagClicked(View view) {
        if (!mEditable) return;
        final InputDialog id = new InputDialog.Builder(mUser.getColor())
                .title(getString(R.string.change_tag))
                .initialText(mUser.getTag())
                .hint(getString(R.string.hint_new_tag))
                .cancelText(getString(R.string.act_cancel))
                .confirmText(getString(R.string.act_confirm))
                .build();
        id.setCallback(new InputDialog.Callback() {
            @Override
            public void onConfirm(String tag) {
                mUser.setTag(tag);
                mTvTag.setText(tag);
                id.dismiss();
            }

            @Override
            public void onCancel() {

            }
        });
        id.show(getFragmentManager(), InputDialog.class.getName());
    }

    public void onRlSexClicked(View view) {
        if (!mEditable) return;
        final String[] items = getResources().getStringArray(R.array.sex);
        ListChooserDialog lcd = new ListChooserDialog.Builder(mUser.getColor())
                .title(getString(R.string.change_sex))
                .items(items)
                .initialIndex(mUser.getSex())
                .callback(new ListChooserDialog.Callback() {
                    @Override
                    public void onItemPicked(int sex) {
                        mUser.setSex(sex);
                        mTvSex.setText(items[sex]);
                    }
                })
                .build();
        lcd.show(getFragmentManager(), ListChooserDialog.class.getName());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


        @Override
    public void finish() {
        if (mEditable) {
            if (App.getMe().equalsEditableData(mUser)) { // 用户未更改个人信息
                super.finish();
                return;
            } else if (!isNetworkAvailable()) {
                showLongToast(R.string.error_cannot_update_user_info_network);
                super.finish();
                return;
            }

            Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
            UserService service = retrofit.create(UserService.class);
            RxHelper.ioMain(service.update(mUser),
                    new SimpleObserver<ResponseBody>() {
                        @Override
                        public void onError(Throwable throwable) {
                            super.onError(throwable);
                            showLongToast(R.string.error_network_disconnected);
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                String result = responseBody.string();
                                if (!Def.Network.SUCCESS.equals(
                                        JsonUtil.getParam(result, Def.Network.STATUS).getAsString())) {
                                    showLongToast(R.string.error_unknown);
                                    return;
                                }

                                App.setMe(mUser);
                                UserDao.getInstance().update(mUser);
                                UserManager.getInstance().saveUserInfoToLocal(mUser);

                                UserActivity.super.finish();
                            } catch (IOException e) {
                                e.printStackTrace();
                                showLongToast(R.string.error_unknown);
                            }
                        }
                    });
        } else {
            super.finish();
        }
    }
}
