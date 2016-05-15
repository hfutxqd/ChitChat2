package com.room517.chitchat.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.room517.chitchat.utils.Logger;

/**
 * Created by ywwynm on 2016/5/14.
 * 一个自定义的Activity类，作为"侃侃"所有Activity的基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Logger mLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogger = Logger.getInstance(getClass());
    }

    protected void init() {
        initMember();
        findViews();
        initUI();
        setupEvents();
    }

    protected abstract void initMember();
    protected abstract void findViews();
    protected abstract void initUI();
    protected abstract void setupEvents();

    @SuppressWarnings("unchecked")
    protected <T extends View> T f(@IdRes int id) {
        return (T) findViewById(id);
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T f(View parent, @IdRes int id) {
        return (T) parent.findViewById(id);
    }
}
