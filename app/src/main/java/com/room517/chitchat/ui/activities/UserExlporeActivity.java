package com.room517.chitchat.ui.activities;

import android.os.Bundle;

import com.room517.chitchat.R;
import com.room517.chitchat.ui.fragments.ExploreListFragment;

public class UserExlporeActivity extends BaseActivity {

    private final static String FRAGMENT_TAG = "UserExlporeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_exlpore);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Bundle data = new Bundle();
        data.putBoolean(ExploreListFragment.ARG_SHOW_SELF, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.user_explore_container, ExploreListFragment.newInstance(data),FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
