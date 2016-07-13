package com.room517.chitchat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.ChatDetail;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.adapters.UserAdapter;

import java.util.List;

public class UserChooserActivity extends BaseActivity {

    private List<User> mUsers;
    private ChatDetail mChatDetail;

    private Toolbar mActionbar;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chooser);

        super.init();
    }

    @Override
    protected void initMember() {
        Intent intent = getIntent();
        mUsers        = intent.getParcelableArrayListExtra(Def.Key.USERS);
        mChatDetail   = intent.getParcelableExtra(Def.Key.CHAT_DETAIL);
    }

    @Override
    protected void findViews() {
        mActionbar = f(R.id.actionbar);

        mRecyclerView = f(R.id.rv_user_chooser);
    }

    @Override
    protected void initUI() {
        setSupportActionBar(mActionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        UserAdapter userAdapter = new UserAdapter(this, mUsers, null);
        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(User user) {
                Intent intent = new Intent(UserChooserActivity.this, ChatDetailsActivity.class);
                intent.putExtra(Def.Key.USER, user);
                intent.putExtra(Def.Key.CHAT_DETAIL, mChatDetail);
                intent.putExtra(Def.Key.CHAT_DETAIL_SCROLL, (ChatDetail) null);
                startActivity(intent);
                finish();
            }
        });
        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
}
