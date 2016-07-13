package com.room517.chitchat.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.room517.chitchat.R;

import xyz.imxqd.licenseview.LicenseView;

public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LicenseView licenseView = new LicenseView(this);
        licenseView.setLicenses(R.xml.licenses);
        setContentView(licenseView);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
