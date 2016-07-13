package com.room517.chitchat.ui.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.fragments.ExploreListFragment;

public class UserExploreActivity extends BaseActivity {

    private final static String FRAGMENT_TAG = "UserExploreActivity";
    public static final String ARG_USER = "user";
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_exlpore);
        initWindow();
        user = getIntent().getParcelableExtra(ARG_USER);
        if(user != null) {
            Bundle data = new Bundle();
            data.putBoolean(ExploreListFragment.ARG_SHOW_SELF, true);
            data.putParcelable(ExploreListFragment.ARG_USER, user);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.user_explore_container, ExploreListFragment.newInstance(data), FRAGMENT_TAG)
                    .commit();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_explore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.act_message) {
            Intent intent = new Intent(this, ChatDetailsActivity.class);
            intent.putExtra(Def.Key.USER, user);
            startActivity(intent);
            return true;
        }
        return false;
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
