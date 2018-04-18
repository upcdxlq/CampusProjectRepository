package com.upc.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.upc.spatialite.activities.SpatialiteMainActivity;
import com.upc.worldwindx.activities.LocationActivity;
import com.upc.worldwindx.activities.WorldwindxMainActivity;
import com.upc.R;
/**
 * Created by Lenovo on 2018/3/28.
 */

public class MainActivity extends Activity  implements View.OnClickListener{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    @Override
    public void  onClick(View v){
          if(v.getId()==R.id.btnOpenWW){
              Intent ww = new Intent(this, WorldwindxMainActivity.class);
              startActivity(ww);
          }else if(v.getId()==R.id.btnOpneSpatialite){
              Intent spatialite = new Intent(this, SpatialiteMainActivity.class);
              startActivity(spatialite);
          }else if(v.getId()==R.id.btnOpenLocation){

              Intent location = new Intent(this, LocationActivity.class);
              startActivity(location);
          }
    }
}
