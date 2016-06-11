package com.room517.chitchat.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.room517.chitchat.R;
import com.room517.chitchat.ui.adapters.BaseViewHolder;

/**
 * Created by ywwynm on 2016/6/9.
 * 使用RadioButton来选择某个选项的dialog
 */
public class ListChooserDialog extends BaseDialog {

    private String[] mItems;
    private int mInitialIndex;

    private int mAccentColor;

    private String mTitle;

    public interface Callback {
        void onItemPicked(int position);
    }

    private Callback mCallback;

    @Override
    protected int getLayoutResource() {
        return R.layout.dialog_list_chooser;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        RecyclerView rv = f(R.id.rv_list_chooser);
        rv.setAdapter(new ListChooserAdapter());
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        initTitle();

        return mContentView;
    }

    private void initTitle() {
        TextView tvTitle = f(R.id.tv_title_list_chooser);
        if (mTitle != null) {
            tvTitle.setText(mTitle);
            tvTitle.setTextColor(mAccentColor);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
    }

    private class ListChooserAdapter extends RecyclerView.Adapter<ListChooserAdapter.Holder> {

        private LayoutInflater mInflater = LayoutInflater.from(getActivity());

        private int mPickedIndex = mInitialIndex;

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(mInflater.inflate(R.layout.rv_list_chooser, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.rb.setText(mItems[position]);
            holder.rb.setChecked(mPickedIndex == position);
        }

        @Override
        public int getItemCount() {
            return mItems.length;
        }

        class Holder extends BaseViewHolder {

            RadioButton rb;

            public Holder(View itemView) {
                super(itemView);

                rb = f(R.id.rb_list_chooser);

                rb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int previousPicked = mPickedIndex;
                        mPickedIndex = getAdapterPosition();
                        notifyItemChanged(previousPicked);

                        if (mCallback != null) {
                            mCallback.onItemPicked(mPickedIndex);
                        }
                        dismiss();
                    }
                });
            }
        }

    }

    public static class Builder {

        private ListChooserDialog mDialog;

        public Builder(int accentColor) {
            mDialog = new ListChooserDialog();
            mDialog.mAccentColor = accentColor;
        }

        public Builder title(String title) {
            mDialog.mTitle = title;
            return this;
        }

        public Builder items(String... items) {
            mDialog.mItems = items;
            return this;
        }

        public Builder initialIndex(int initialIndex) {
            mDialog.mInitialIndex = initialIndex;
            return this;
        }

        public Builder callback(Callback callback) {
            mDialog.mCallback = callback;
            return this;
        }

        public ListChooserDialog build() {
            return mDialog;
        }

    }
}
