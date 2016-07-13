package com.room517.chitchat.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.room517.chitchat.App;
import com.room517.chitchat.R;
import com.room517.chitchat.helpers.AMapLocationHelper;
import com.room517.chitchat.helpers.OpenMapHelper;
import com.room517.chitchat.ui.adapters.LocationInforNearbyAdapter;

public class LocationInforActivity extends AppCompatActivity implements AMapLocationHelper.AddrPointInfoCallBack, AdapterView.OnItemClickListener {
    public static final String ARG_TITLE = "title";
    public static final String ARG_LATITUDE = "latitude";
    public static final String ARG_LONGITUDE = "longitude";

    private TextView tvTitle, tvAddress;
    private ProgressBar pbProgress;
    private ListView lvList;
    private LocationInforNearbyAdapter adapter;

    private double dLatitude;
    private double dLongitude;
    private String sTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_infor);
        initData();
        findViews();
        initViews();
        setup();
    }

    private void initData() {
        sTitle = getIntent().getStringExtra(ARG_TITLE);
        dLatitude = getIntent().getDoubleExtra(ARG_LATITUDE, 0);
        dLongitude = getIntent().getDoubleExtra(ARG_LONGITUDE, 0);
        adapter = new LocationInforNearbyAdapter();
    }


    private void findViews() {
        tvTitle = (TextView) findViewById(R.id.location_infor_title);
        tvAddress = (TextView) findViewById(R.id.location_infor_address);
        pbProgress = (ProgressBar) findViewById(R.id.location_infor_progressbar);
        lvList = (ListView) findViewById(R.id.location_infor_list);
    }

    private void initViews() {
        lvList.setAdapter(adapter);
        lvList.setEmptyView(pbProgress);
        tvTitle.setText(sTitle);
    }

    private void setup() {
        lvList.setOnItemClickListener(this);
        App.getLocationHelper().setAddrPointInfoCallBack(this);
        App.getLocationHelper().getAddrPointInfo(dLatitude, dLongitude);
    }

    public void onCloseClick(View view) {
        finish();
    }

    public void onOpenInMapClick(View view) {
        OpenMapHelper.open(dLongitude, dLatitude, sTitle);
    }

    @Override
    public void onGetAddrPonitInfoFinish(RegeocodeResult regeocodeResult) {
        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
        adapter.set(address.getPois());
        adapter.notifyDataSetChanged();
        tvAddress.setText(address.getFormatAddress());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PoiItem item = (PoiItem) adapter.getItem(position);
        OpenMapHelper.open(item.getLatLonPoint().getLongitude(),
                item.getLatLonPoint().getLatitude(), item.getTitle());
    }
}
