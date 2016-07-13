package com.ywwynm.emoji;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;

/**
 * Created by ywwynm on 2016/7/13.
 * EmojiTextView
 */
public class EmojiTextView extends AppCompatTextView {

    private int mEmojiSize;

    public EmojiTextView(Context context) {
        super(context);
        init();
    }

    public EmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmojiTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mEmojiSize = (int) (getTextSize() + 50);
    }

    public void setEmojiSize(int emojiSizePx) {
        mEmojiSize = emojiSizePx;
        setText(getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            EmojiHandler.addEmojis(getContext(), builder,
                    mEmojiSize, DynamicDrawableSpan.ALIGN_BASELINE, (int) getTextSize());
            text = builder;
        }
        super.setText(text, type);
    }
}
