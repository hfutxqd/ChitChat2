package com.room517.chitchat.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.room517.chitchat.R;
import com.room517.chitchat.model.Explore;

import java.util.ArrayList;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈动态列表的适配器
 */
public class ExploreListAdapter extends RecyclerView.Adapter<ExploreListAdapter.ExploreHolder>{
    private ArrayList<Explore> mList;

    public ExploreListAdapter()
    {
        mList = new ArrayList<>();
    }

    public void set(ArrayList<Explore> list)
    {
        mList.clear();
        mList.addAll(list);
    }

    public void put(ArrayList<Explore> list)
    {
        mList.addAll(list);
    }

    @Override
    public ExploreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExploreHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.explore_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ExploreHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ExploreHolder extends RecyclerView.ViewHolder{

        public ExploreHolder(View itemView) {
            super(itemView);
        }
    }
}
