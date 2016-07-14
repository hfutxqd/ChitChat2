package com.room517.chitchat.ui.fragments;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.RxBus;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.utils.DisplayUtil;
import com.ywwynm.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by imxqd on 2016/7/14.
 * Emoji表情键盘
 */
public class EmojiKeyboardFragment extends BaseFragment{

    private RecyclerView emojiList;
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_emoji_keyboard;
    }

    @Override
    protected void findViews() {
        emojiList = f(R.id.emoji_list);
    }

    @Override
    protected void initUI() {
        ArrayList<String> emojiBd = new ArrayList<>(81);
        for (int i = 1; i < 82; i++) {
            emojiBd.add(String.format(Locale.CHINESE, "[%02d]", i));
        }
        emojiList.setAdapter(new EmojiAdapter(emojiBd));
        emojiList.setLayoutManager(new GridLayoutManager(getContext(), 7));
    }

    public static class EmojiAdapter extends RecyclerView.Adapter<EmojiViewHolder> {
        private ArrayList<String> list;

        public EmojiAdapter(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public EmojiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new EmojiViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.rv_emoji_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final EmojiViewHolder holder, int position) {
            holder.emoji.setText(list.get(position));
            holder.emoji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    RxBus.get().post(Def.Event.INPUT_EMOJI, list.get(pos));
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    public static class EmojiViewHolder extends RecyclerView.ViewHolder {
        EmojiTextView emoji;
        public EmojiViewHolder(View itemView) {
            super(itemView);
            emoji = (EmojiTextView) itemView.findViewById(R.id.emoji_item);
            emoji.setEmojiSize(DisplayUtil.dp2px(32));
        }
    }
}
