package com.room517.chitchat.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.room517.chitchat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imxqd on 2016/7/12.
 * 位置点附近信息的适配器
 */
public class LocationInforNearbyAdapter extends BaseAdapter{

    private List<PoiItem> list;

    public LocationInforNearbyAdapter() {
        list = new ArrayList<>(5);
    }

    public void set(List<PoiItem> items) {
        list.clear();
        list.addAll(items);
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

        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.location_infor_nearby_item, parent, false);
        }
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView distance= (TextView) convertView.findViewById(R.id.distance);
        PoiItem item = list.get(position);
        title.setText(item.getTitle());
        distance.setText(parent.getResources().getString(R.string.location_infor_distance, item.getDistance()));
        return convertView;
    }
}
