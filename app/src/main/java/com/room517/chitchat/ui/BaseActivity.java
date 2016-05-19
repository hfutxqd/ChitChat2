package com.room517.chitchat.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.room517.chitchat.utils.DeviceUtil;
import com.room517.chitchat.utils.ClassLogger;

import java.util.HashMap;

/**
 * Created by ywwynm on 2016/5/14.
 * 一个自定义的Activity类，作为"侃侃"所有Activity的基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected ClassLogger mLogger;

    private HashMap<Integer, PermissionCallback> mCallbackMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogger = ClassLogger.getInstance(getClass());
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

    protected void showShortToast(@StringRes int stringRes) {
        Toast.makeText(this, stringRes, Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(@StringRes int stringRes) {
        Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show();
    }

    protected void doWithPermissionChecked(
            PermissionCallback permissionCallback, int requestCode, String... permissions) {
        if (permissionCallback == null) {
            return;
        }

        if (DeviceUtil.hasMarshmallowApi()) {
            if (mCallbackMap == null) {
                mCallbackMap = new HashMap<>();
            }
            for (String permission : permissions) {
                int pg = ContextCompat.checkSelfPermission(this, permission);
                if (pg != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        permissionCallback.onShouldShowExplanation();
                    } else {
                        mCallbackMap.put(requestCode, permissionCallback);
                        ActivityCompat.requestPermissions(this, permissions, requestCode);
                    }
                    return;
                }
            }
        }

        permissionCallback.onGranted();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionCallback callback = mCallbackMap.get(requestCode);
        if (callback != null) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    callback.onDenied();
                    return;
                }
            }
            callback.onGranted();
        }
    }

    protected interface PermissionCallback {
        void onShouldShowExplanation();
        void onGranted();
        void onDenied();
    }

    protected class SimplePermissionCallback implements PermissionCallback {

        @Override
        public void onShouldShowExplanation() { }

        @Override
        public void onGranted() { }

        @Override
        public void onDenied() { }
    }
}
