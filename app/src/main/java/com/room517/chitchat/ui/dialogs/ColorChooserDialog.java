package com.room517.chitchat.ui.dialogs;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.room517.chitchat.R;
import com.room517.chitchat.ui.adapters.BaseViewHolder;

/**
 * Created by ywwynm on 2016/6/8.
 * 选择颜色的dialog
 */
public class ColorChooserDialog extends BaseDialog {

    private int mAccentColor;
    private int[] mColors;

    private String mTitle;

    private int mInitialColor;

    public interface Callback {
        void onColorPicked(int color);
    }

    private Callback mCallback;

    @Override
    protected int getLayoutResource() {
        return R.layout.dialog_color_chooser;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mColors = getResources().getIntArray(R.array.material_500);

        RecyclerView rv = f(R.id.rv_color_chooser);
        rv.setAdapter(new ColorChooserAdapter());
        rv.setLayoutManager(new GridLayoutManager(getActivity(), 4));

        initTitle();

        return mContentView;
    }

    private void initTitle() {
        TextView tvTitle = f(R.id.tv_title_color_chooser);
        if (mTitle != null) {
            tvTitle.setText(mTitle);
            tvTitle.setTextColor(mAccentColor);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
    }

    private class ColorChooserAdapter extends RecyclerView.Adapter<ColorChooserAdapter.Holder> {

        LayoutInflater mInflater = LayoutInflater.from(getActivity());

        int mPickedColor = mInitialColor;
        int mPickedIndex = getColorIndex(mPickedColor);

        private int getColorIndex(int color) {
            for (int i = 0; i < mColors.length; i++) {
                if (mColors[i] == color) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(mInflater.inflate(R.layout.rv_color_chooser, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.fab.setBackgroundTintList(ColorStateList.valueOf(mColors[position]));
            if (mPickedIndex == position) {
                holder.fab.setImageResource(R.drawable.ic_picked_white);
            } else {
                holder.fab.setImageResource(0);
            }
        }

        @Override
        public int getItemCount() {
            return mColors.length;
        }

        class Holder extends BaseViewHolder {

            FloatingActionButton fab;

            public Holder(View itemView) {
                super(itemView);

                fab = f(R.id.fab_color_chooser);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        mPickedIndex = pos;
                        mPickedColor = mColors[pos];
                        notifyItemChanged(pos);

                        if (mCallback != null) {
                            mCallback.onColorPicked(mPickedColor);
                        }
                        dismiss();
                    }
                });
            }
        }

    }

    public static class Builder {

        private ColorChooserDialog mDialog;

        public Builder(int accentColor) {
            mDialog = new ColorChooserDialog();
            mDialog.mAccentColor = accentColor;
        }

        public Builder title(String title) {
            mDialog.mTitle = title;
            return this;
        }

        public Builder callback(Callback callback) {
            mDialog.mCallback = callback;
            return this;
        }

        public Builder initialColor(int color) {
            mDialog.mInitialColor = color;
            return this;
        }

        public ColorChooserDialog build() {
            return mDialog;
        }

    }

}
