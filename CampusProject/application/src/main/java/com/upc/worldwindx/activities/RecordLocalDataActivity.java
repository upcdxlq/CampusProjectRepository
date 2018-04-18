package com.upc.worldwindx.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.upc.R;
import com.upc.spatialite.service.SpatialiteService;


/**
 * Created by Lenovo on 2018/4/5.
 */

public class RecordLocalDataActivity extends AppCompatActivity {
  /*  private AMapLocationClient mLocationClient =null;
    private AMapLocationClientOption mLocationOption = null;*/

    EditText txtname =null ;
    EditText txttype = null;
    EditText txtlatitude =null;
    EditText txtlongitude = null;
    EditText txtdescription = null;
    private Button btnRecordLocal = null;
    TextView txtResult =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_localdata_layout);
        txtname = (EditText)findViewById(R.id.name);
        txttype = (EditText)findViewById(R.id.type);
        txtlatitude =(EditText)findViewById(R.id.latitude);
        txtlongitude = (EditText)findViewById(R.id.longitude);
        txtdescription = (EditText)findViewById(R.id.description);
        btnRecordLocal = (Button)findViewById(R.id.btnRecordLocal);
        txtResult = (TextView)findViewById(R.id.Result);


    }

    //声明定位回调监听器
/*    AMapLocationListener mLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
    //可在其中解析amapLocation获取相应内容。
                    double latitude = amapLocation.getLatitude();
                    double longitude = amapLocation.getLongitude();
                    String address = amapLocation.getAddress();

                    txtlatitude.setText(String.valueOf(latitude));
                    txtlongitude.setText(String.valueOf(longitude));
                    txtdescription.setText(String.valueOf(address));

                    mLocationClient.stopLocation();
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                     TextView txtResult =(TextView)findViewById(R.id.Result);
                     txtResult.setText("location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };*/



    @Override
    protected void onStart() {
        super.onStart();

/**
 * gaode  location
 */

        /*//初始化定位
        mLocationClient  = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
       //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
       //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
*/
        /**
         * baidu location
         */
        final LocationClient client = new LocationClient(getApplicationContext());
        LocationClientOption mOption = new LocationClientOption();
        mOption.setScanSpan(2000);
        mOption.setOpenGps(true);
        mOption.setIsNeedAltitude(true);
        mOption.setIsNeedAddress(true);
        BDAbstractLocationListener locationListener = new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                    //可在其中解析amapLocation获取相应内容。
                    bdLocation.setLatitude(bdLocation.getLatitude()-0.000176);
                    bdLocation.setLongitude(bdLocation.getLongitude()-0.005414);
                            double latitude = bdLocation.getLatitude();
                            double longitude = bdLocation.getLongitude();
                            String address = bdLocation.getAddrStr();
                            txtlatitude.setText(String.valueOf(latitude));
                            txtlongitude.setText(String.valueOf(longitude));
                            txtname.setText(String.valueOf(address));
                            txtdescription.setText(String.valueOf(address));

                            client.stop();
                        }else {
                            //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                            Log.e("AmapError","location Error, ErrCode:"
                                    + bdLocation.getLocType());
                            TextView txtResult =(TextView)findViewById(R.id.Result);
                            txtResult.setText("location Error, ErrCode:"
                                    + bdLocation.getLocType());
                        }
                    }
            };
        client.setLocOption(mOption);
        client.registerLocationListener(locationListener);
        client.start();


        btnRecordLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin record module
              int recordsNumber = SpatialiteService.getRecordsNumber(getApplicationContext(),"Select count(*) FROM poiSet;");
                if(recordsNumber>=0) {
                    //String sql = "INSERT INTO poiSet(pid,name,type,description,geometry) VALUES (" + recordsNumber + ", '" + txtname.getText() + "', '" + txttype.getText() + "','" + txtdescription.getText().toString() +"', GeomFromText('POINT(" + Double.parseDouble(txtlatitude.getText().toString()) + " " +  Double.parseDouble(txtlongitude.getText().toString()) + ")', " + 2401 + ")"+ ");";
                    //String sql = "INSERT INTO poiSet(pid,name,type,description,geometry) VALUES (" + recordsNumber + ", '" + txtname.getText() + "', '" + txttype.getText() + "','" + txtdescription.getText().toString() +"', NULL );";
                    int fid = recordsNumber;
                    String name = txtname.getText().toString();
                    String type = txttype.getText().toString();
                    String description = txtdescription.getText().toString();
                    String geometry = "POINT("+Double.parseDouble(txtlongitude.getText().toString())+" "+Double.parseDouble(txtlatitude.getText().toString())+")";
                    boolean insertSuccess = SpatialiteService.insertLocalDB(getApplicationContext(),name,type,description,geometry);
                    if (insertSuccess) {
                        txtResult.setText("record success !\n");
                        try {
                        String sql = "SELECT fid,name,type,description,ST_AsText(the_geom) FROM test_geom;";
                        String result = SpatialiteService.SelectData(getApplicationContext(),sql);
                        txtResult.append(result);
                        }catch (Exception e){
                        }
                        }
            }
         }
        });
        }

}
