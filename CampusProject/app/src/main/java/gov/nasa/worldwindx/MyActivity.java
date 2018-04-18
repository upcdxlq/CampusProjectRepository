/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Lenovo on 2018/3/20.
 */


public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_activity);
        addFragment();

    }

    private  void addFragment(){
        Fragment globe = new BasicGlobeFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.globe_container, globe)    // replace (destroy) existing fragment (if any)
            .commit();
    }

}
