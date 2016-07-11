package com.room517.chitchat.ui.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.room517.chitchat.R;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.fragments.ExploreListFragment;

public class UserExlporeActivity extends BaseActivity {

    private final static String FRAGMENT_TAG = "UserExlporeActivity";
    public static final String ARG_USER = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_exlpore);
        initWindow();
        User user = getIntent().getParcelableExtra(ARG_USER);
        if(user != null) {
            Bundle data = new Bundle();
            data.putBoolean(ExploreListFragment.ARG_SHOW_SELF, true);
            data.putParcelable(ExploreListFragment.ARG_USER, user);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.user_explore_container, ExploreListFragment.newInstance(data), FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @TargetApi(19)
    private void initWindow() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
