package com.room517.chitchat.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by ywwynm on 2016/5/17.
 * 关于显示的类
 */
public class DisplayUtil {

    private static float density = App.getApp().getResources().getDisplayMetrics().density;

    /**
     * 将dp值转换为对应的px值
     * @param dp 待转换的dp值
     * @return 转换后的px值
     */
    public static int dp2px(int dp) {
        return (int) (dp * density);
    }

    /**
     * @return 设备屏幕的真实宽高
     */
    public static Point getScreenSize() {
        Point screen = new Point();
        Display display = ((WindowManager) App.getApp().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                screen.x = (Integer) mGetRawW.invoke(display);
                screen.y = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                display.getSize(screen);
                Logger.e("Cannot use reflection to get real screen size. " +
                        "Returned size may be wrong.");
            }
        } else {
            display.getRealSize(screen);
        }
        return screen;
    }

    /**
     * @return 状态栏高度
     */
    public static int getStatusbarHeight() {
        Resources resources = App.getApp().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else return 0;
    }

    /**
     * @return 如果设备有虚拟导航栏，返回{@code true}；否则，返回{@code false}
     */
    public static boolean hasNavigationBar() {
        boolean hasMenuKey = ViewConfiguration.get(App.getApp()).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        return !hasMenuKey && !hasBackKey;
    }

    /**
     * @return 虚拟导航栏的高度
     */
    public static int getNavigationBarHeight() {
        Resources resources = App.getApp().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else return 0;
    }

    /**
     * 为{@param editText}设置文字选择时开始、结束标志的颜色
     */
    public static void setSelectionHandlersColor(EditText editText, int color) {
        try {
            final Class<?> cTextView = TextView.class;
            final Field fhlRes = cTextView.getDeclaredField("mTextSelectHandleLeftRes");
            final Field fhrRes = cTextView.getDeclaredField("mTextSelectHandleRightRes");
            final Field fhcRes = cTextView.getDeclaredField("mTextSelectHandleRes");
            fhlRes.setAccessible(true);
            fhrRes.setAccessible(true);
            fhcRes.setAccessible(true);

            int hlRes = fhlRes.getInt(editText);
            int hrRes = fhrRes.getInt(editText);
            int hcRes = fhcRes.getInt(editText);

            final Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            final Object editor = fEditor.get(editText);

            final Class<?> cEditor = editor.getClass();
            final Field fSelectHandleL = cEditor.getDeclaredField("mSelectHandleLeft");
            final Field fSelectHandleR = cEditor.getDeclaredField("mSelectHandleRight");
            final Field fSelectHandleC = cEditor.getDeclaredField("mSelectHandleCenter");
            fSelectHandleL.setAccessible(true);
            fSelectHandleR.setAccessible(true);
            fSelectHandleC.setAccessible(true);

            Drawable selectHandleL = ContextCompat.getDrawable(editText.getContext(), hlRes);
            Drawable selectHandleR = ContextCompat.getDrawable(editText.getContext(), hrRes);
            Drawable selectHandleC = ContextCompat.getDrawable(editText.getContext(), hcRes);

            selectHandleL.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            selectHandleR.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            selectHandleC.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);

            fSelectHandleL.set(editor, selectHandleL);
            fSelectHandleR.set(editor, selectHandleR);
            fSelectHandleC.set(editor, selectHandleC);
        } catch (Exception ignored) { }
    }

    public static int getLightColor(int mdColor500) {
        int[] colorArr = App.getApp().getResources().getIntArray(R.array.material_500);
        int[] lightArr = App.getApp().getResources().getIntArray(R.array.material_100);
        for (int i = 0; i < colorArr.length; i++) {
            if (colorArr[i] == mdColor500) {
                return lightArr[i];
            }
        }
        return lightArr[0];
    }

}
