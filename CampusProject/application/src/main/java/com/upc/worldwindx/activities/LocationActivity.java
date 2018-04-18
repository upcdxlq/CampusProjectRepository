package com.upc.worldwindx.activities;


import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.upc.R;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/**
 * Created by Lenovo on 2018/3/28.
 */

public class LocationActivity extends Activity {

    public class LocListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            String str = "当前位置:\n";
            double latitude;
            double longitude;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            str  = str+"纬度："+latitude+"\n经度： " +longitude;
            Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getApplicationContext(),"状态改变！",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(),"位置提供者被启用！",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),"位置提供者被禁用！",Toast.LENGTH_LONG).show();
        }
    }
    LocationManager lm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_layout);
        Button btn = (Button)findViewById(R.id.btnlocation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        /*Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        LocationProvider lp = lm.getProvider(LocationManager.GPS_PROVIDER);*/
                        if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                           /* Toast.makeText(getApplicationContext(),"NetWork abled",Toast.LENGTH_LONG).show();*/
                                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocListener());
                                Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (location == null) {
                                  /*  Toast.makeText(getApplicationContext(), "update location", Toast.LENGTH_LONG).show();*/
                                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocListener());
                                } else {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    String str = "当前位置\n纬度：" + latitude + "\n经度： " + longitude;
                                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                                }
                        }else{
                            Toast.makeText(getApplicationContext(),"NetWork Unabled",Toast.LENGTH_LONG).show();
                        }

                }catch (SecurityException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
