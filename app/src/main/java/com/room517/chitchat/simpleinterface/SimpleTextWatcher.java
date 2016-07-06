package com.room517.chitchat.simpleinterface;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by ywwynm on 2016/7/6.
 * 一个{@link TextWatcher}的空实现类
 */
public class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
