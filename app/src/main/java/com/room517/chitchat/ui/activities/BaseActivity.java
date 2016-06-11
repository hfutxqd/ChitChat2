package com.room517.chitchat.ui.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.room517.chitchat.R;
import com.room517.chitchat.utils.DeviceUtil;

import java.util.HashMap;

/**
 * Created by ywwynm on 2016/5/14.
 * 一个自定义的Activity类，作为"侃侃"所有Activity的基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private HashMap<Integer, PermissionCallback> mCallbackMap;

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

    public void showShortToast(@StringRes int stringRes) {
        Toast.makeText(this, stringRes, Toast.LENGTH_SHORT).show();
    }

    public void showShortToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void showLongToast(@StringRes int stringRes) {
        Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show();
    }

    public void showLongToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    /**
     * 检查权限赋予情况并执行某个操作
     * 这里没有实现{@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)}的相关
     * 逻辑，因为若要实现，就必须在解释完为什么需要权限后，再申请一次，处理这种情况会比较麻烦
     * @param permissionCallback 权限请求的回调
     * @param requestCode 请求码
     * @param permissions 请求的权限
     */
    public void doWithPermissionChecked(
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
                    mCallbackMap.put(requestCode, permissionCallback);
                    ActivityCompat.requestPermissions(this, permissions, requestCode);
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

    public interface PermissionCallback {
        void onGranted();
        void onDenied();
    }

    /**
     * 一个实现了{@link PermissionCallback}接口的类，简单地处理了权限被拒绝授予的情况，
     * 使用时可以不用每次都要实现两个方法了
     */
    public class SimplePermissionCallback implements PermissionCallback {

        @Override
        public void onGranted() { }

        @Override
        public void onDenied() {
            showLongToast(R.string.error_permission_not_granted);
        }
    }
}
