package com.room517.chitchat.utils;

import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by imxqd on 2016/6/23.
 * 用于处理View的单击,双击事件
 */
public class ClickEventHelper implements View.OnTouchListener{
    private ClickListener mListener = null;

    public ClickEventHelper(ClickListener listener){
        mListener = listener;
    }

    public void attachTo(View view) {
        view.setOnTouchListener(this);
    }

    public void unAttachTo(View view){
        view.setOnTouchListener(null);
    }

    private boolean clicked = false;
    private void handleEvent(final View view) {
        Timer timer;
        if (!clicked) {
            clicked = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(clicked){
                        mListener.onOneClick(view);
                    }
                    clicked = false;
                }
            }, 300);
        } else {
            clicked = false;
            mListener.onDoubleClick(view);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                handleEvent(v);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;//返回false确保已绑定的其他的事件正常工作
    }

    public interface ClickListener{
        void onOneClick(View view);
        void onDoubleClick(View view);
    }
}
