package com.room517.chitchat.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.User;
import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.DisplayUtil;

public class UserActivity extends BaseActivity {

    private User    mUser;
    private boolean mEditable;

    private Toolbar mActionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        super.init();
    }

    @Override
    protected void initMember() {
        mUser = getIntent().getParcelableExtra(Def.Key.USER);
        mEditable = mUser.getId().equals(App.getMe().getId());
    }

    @Override
    protected void findViews() {
        mActionbar = f(R.id.actionbar);
    }

    @Override
    protected void initUI() {
        if (DeviceUtil.hasLollipopApi()) {
            getWindow().setStatusBarColor(DisplayUtil.getDarkColor(mUser.getColor()));
        }
        initActionbar();
    }

    private void initActionbar() {
        mActionbar.setBackgroundColor(mUser.getColor());
        setSupportActionBar(mActionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mUser.getName());
        }
    }

    @Override
    protected void setupEvents() {

    }
}
