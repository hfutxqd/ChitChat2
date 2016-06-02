package com.room517.chitchat.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.R;

import java.util.List;

/**
 * Created by ywwynm on 2016/6/2.
 * 在一个列表中显示一些操作
 */
public class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.SimpleListHolder> {

    private LayoutInflater mInflater;
    private List<String>   mItems;

    public SimpleListAdapter(Activity activity, List<String> items) {
        mInflater = LayoutInflater.from(activity);
        mItems = items;
    }

    @Override
    public SimpleListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleListHolder(mInflater.inflate(R.layout.rv_simple_list, parent, false));
    }

    @Override
    public void onBindViewHolder(SimpleListHolder holder, int position) {
        holder.tv.setText(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class SimpleListHolder extends BaseViewHolder {

        TextView tv;

        public SimpleListHolder(View itemView) {
            super(itemView);

            tv = f(R.id.tv_simple_list);

            Logger.i("itemView type in SimpleListAdapter: " + itemView.toString());
        }
    }

}
