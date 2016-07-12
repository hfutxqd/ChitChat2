package com.room517.chitchat.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.room517.chitchat.R;

import java.util.ArrayList;

/**
 * Created by imxqd on 2016/7/11.
 * 位置点选择列表的适配器
 */
public class LocationListAdapter extends BaseAdapter {

    private ArrayList<PoiItem> list;
    public LocationListAdapter() {
        list = new ArrayList<>(5);
    }

    public void set(ArrayList<PoiItem> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.location_list_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        holder = (ViewHolder) convertView.getTag();
        PoiItem item = list.get(position);
        StringBuilder addr = new StringBuilder(item.getProvinceName());
        addr.append(item.getCityName());
        addr.append(item.getAdName());
        addr.append(item.getBusinessArea());
        holder.title.setText(item.getTitle());
        holder.addr.setText(addr);
        return convertView;
    }

    public static class ViewHolder {
        TextView title, addr;
        public ViewHolder(View v) {
            title = (TextView) v.findViewById(R.id.location_list_item_title);
            addr = (TextView) v.findViewById(R.id.location_list_item_address);
        }
    }
}
