package com.room517.chitchat.utils;

import android.animation.Animator;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;

/**
 * Created by imxqd on 2016/7/9.
 * View动画工具类
 */
public class ViewAnimationUtil {
    public static void scaleOut(View view, @Nullable Callback callback) {
        view.animate()
                .scaleX(0)
                .scaleY(0)
                .setListener(callback)
                .setDuration(200)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .start();
    }

    public static void scaleIn(View view, @Nullable Callback callback) {
        view.animate()
                .scaleX(1)
                .scaleY(1)
                .setListener(callback)
                .setDuration(200)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .start();
    }

    public static abstract class Callback implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
