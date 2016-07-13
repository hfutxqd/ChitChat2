package com.ywwynm.emoji;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;

/**
 * Created by ywwynm on 2016/7/13.
 * EmojiEditText
 */
public class EmojiEditText extends AppCompatEditText {

    private int mEmojiSize;

    public EmojiEditText(Context context) {
        super(context);
        init();
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmojiEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mEmojiSize = (int) (getTextSize() + 50);
    }

    public void setEmojiSize(int emojiSizePx) {
        mEmojiSize = emojiSizePx;
        updateForEmoji();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        updateForEmoji();
    }

    private void updateForEmoji() {
        EmojiHandler.addEmojis(
                getContext(), getText(), mEmojiSize,
                DynamicDrawableSpan.ALIGN_BASELINE, (int) getTextSize());
    }

}
