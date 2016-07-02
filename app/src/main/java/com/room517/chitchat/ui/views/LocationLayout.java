package com.room517.chitchat.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.room517.chitchat.R;

/**
 * Created by imxqd on 2016/7/2.
 * 用于显示位置信息的View
 */
public class LocationLayout extends LinearLayout{
    TextView text = null;
    ImageView icon = null;

    public LocationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        text = (TextView) findViewById(R.id.location_view_text);
        icon = (ImageView) findViewById(R.id.location_view_icon);
    }

    public LocationLayout(Context context) {
        super(context);
    }

    public TextView getTextView(){
        if(text == null){
            text = (TextView) findViewById(R.id.location_view_text);
        }
        return text;
    }

    public ImageView getImageView(){
        if(icon == null){
            icon = (ImageView) findViewById(R.id.location_view_icon);
        }
        return icon;
    }

    public void setText(String str){
        if(text == null){
            text = (TextView) findViewById(R.id.location_view_text);
        }
        text.setText(str);
    }
}
