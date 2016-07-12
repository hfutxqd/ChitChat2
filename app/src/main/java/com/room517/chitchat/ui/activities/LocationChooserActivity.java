package com.room517.chitchat.ui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.room517.chitchat.App;
import com.room517.chitchat.R;
import com.room517.chitchat.helpers.AMapLocationHelper;
import com.room517.chitchat.ui.adapters.LocationListAdapter;

public class LocationChooserActivity extends AppCompatActivity implements AMapLocationHelper.AMapLocationCallBack,
        AMapLocationHelper.AddrPonitsCallBack, AdapterView.OnItemClickListener {

    public static final String ARG_POIITEM = "poiitem";
    private ListView listView;
    private LocationListAdapter adapter;
    private AMapLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_chooser);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        listView = (ListView) findViewById(R.id.location_list);
        listView.addHeaderView(LayoutInflater.from(this)
        .inflate(R.layout.location_list_header, null));
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.location_empty);
        listView.setEmptyView(progressBar);
        adapter = new LocationListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        App.getLocationHelper().getLocation(this);
    }


    @Override
    public void onAMapLocationFinish(AMapLocation location) {
        this.location = location;
        App.getLocationHelper().setAddrPonitsCallBack(this);
        App.getLocationHelper().getAddrPonits(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onGetAddrPonitsFinish(PoiResult poiResult) {
        adapter.set(poiResult.getPois());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        if(id < 0) {
            PoiItem item = new PoiItem("",
                    new LatLonPoint(location.getLatitude(),location.getLongitude()), "", "");
            intent.putExtra(ARG_POIITEM, item);
            setResult(RESULT_OK, intent);
        } else {
            PoiItem item = (PoiItem) adapter.getItem((int) id);
            intent.putExtra(ARG_POIITEM, item);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
