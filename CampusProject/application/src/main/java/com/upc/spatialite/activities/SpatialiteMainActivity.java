package com.upc.spatialite.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.upc.R;

/**
 * Created by Lenovo on 2018/3/28.
 */

public class SpatialiteMainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spatialite_main_layout);
    }
}
