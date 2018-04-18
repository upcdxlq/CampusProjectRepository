/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package com.upc.worldwindx.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.BasicElevationCoverage;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.NASA_SRTM30_TiledElevationCoverage;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.TiandituCia_cLayer;
import gov.nasa.worldwind.layer.TiandituCva_cLayer;
import gov.nasa.worldwind.layer.TiandituImg_cLayer;
import gov.nasa.worldwind.layer.TiandituVec_cLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Placemark;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.upc.R;
import com.upc.worldwindx.activities.AddPlacemarkActivity;
import com.upc.worldwindx.activities.CodeActivity;
import com.upc.worldwindx.activities.LocationActivity;

public class BasicGlobeFragment extends Fragment {

    private WorldWindow wwd;

    public BasicGlobeFragment(){
    }

    /**
     * Creates a new WorldWindow (GLSurfaceView) object.
     */
    public WorldWindow createWorldWindow() {
        // Create the World Window (a GLSurfaceView) which displays the globe.
        this.wwd = new WorldWindow(getContext());
        // Setup the World Window's layers.
        this.wwd.getLayers().addLayer(new BackgroundLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        // Setup the World Window's elevation coverages.
        this.wwd.getGlobe().getElevationModel().addCoverage(new BasicElevationCoverage());
        return this.wwd;
    }

    /**
     * Gets the WorldWindow (GLSurfaceView) object.
     */
    public WorldWindow getWorldWindow() {
        return this.wwd;
    }

    /**
     * Adds the WorldWindow to this Fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // add fragment layout
        View rootView = inflater.inflate(R.layout.globe_fragment_layout, container, false);
        FrameLayout globeLayout = (FrameLayout) rootView.findViewById(R.id.globe);
        // Add the WorldWindow view object to the layout that was reserved for the globe.
        globeLayout.addView(this.createWorldWindow());

        //  to trace current location and mark it
        traceCurrentLocation();
        // change map type to show
        final Button btnToggle =(Button)rootView.findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(btnToggle.getText().toString().equals("三维")){
                    getWorldWindow().getLayers().clearLayers();
                    getWorldWindow().getLayers().addLayer(new TiandituImg_cLayer());
                    getWorldWindow().getLayers().addLayer(new TiandituCia_cLayer());
                    getWorldWindow().getGlobe().getElevationModel().clearCoverages();
                    getWorldWindow().getGlobe().getElevationModel().addCoverage(new NASA_SRTM30_TiledElevationCoverage());
                    getstartLocation(3);

                    btnToggle.setText("在线地图");
                }else if(btnToggle.getText().equals("在线地图")){
                    getWorldWindow().getLayers().clearLayers();
                    getWorldWindow().getLayers().addLayer(new TiandituVec_cLayer());
                    getWorldWindow().getLayers().addLayer(new TiandituCva_cLayer());
                    getWorldWindow().getGlobe().getElevationModel().clearCoverages();
                    getWorldWindow().getGlobe().getElevationModel().addCoverage(new BasicElevationCoverage());
                    getstartLocation(2);

                    btnToggle.setText("三维");
                }
            }
        });

        Button btnAddMark = (Button)rootView.findViewById(R.id.btnAddMark);
        btnAddMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             double latitude =   getWorldWindow().getNavigator().getLatitude();
             double longitude =  getWorldWindow().getNavigator().getLongitude();
             Context context = getContext();
             Intent intent = new Intent(context, AddPlacemarkActivity.class);
             Bundle bundle = new Bundle();
             bundle.putDouble("latitude", latitude);
             bundle.putDouble("longitude",longitude);
             intent.putExtra("arguments", bundle);
             context.startActivity(intent);
            }
        });
        return rootView;
    }
    /**
     * Resumes the WorldWindow's rendering thread
     */
    @Override
    public void onResume() {
        super.onResume();
        this.wwd.onResume(); // resumes a paused rendering thread
    }

    /**
     * Pauses the WorldWindow's rendering thread
     */
    @Override
    public void onPause() {
        super.onPause();
        this.wwd.onPause(); // pauses the rendering thread
    }

    public Position getstartLocation(int type){

        final int mapType = type;
        final Position currentPosition = new Position();
// gaode location
   /*     final AMapLocationClient mLocationClient  = new AMapLocationClient(getActivity().getApplicationContext());
        //声明定位回调监听器
        final AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {

                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //可在其中解析amapLocation获取相应内容。
                       double latitude = amapLocation.getLatitude();
                       double longitude = amapLocation.getLongitude();
                       String address = amapLocation.getAddress();
                       double altitude= amapLocation.getAltitude();
                       currentPosition.set(latitude,longitude,altitude);
                        //just init location  once
                        amapLocation.setLongitude(amapLocation.getLongitude()-0.0054);
                        startPositionView(amapLocation,mapType);
                        mLocationClient.stopLocation();

                        // Toast.makeText(getApplicationContext(),address+"\n经度："+longitude +"\n纬度:"+latitude,Toast.LENGTH_LONG);
                    }
                }
            }
        };

        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        // mLocationOption.setInterval(2000);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();*/

        //baidu location
        /**mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        mOption.setScanSpan(3000);//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
        mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
        mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mOption.setOpenGps(true);//可选，默认false，设置是否开启Gps定位
        mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用**/

        //baidu location
        final LocationClient client = new LocationClient(getActivity().getApplicationContext());
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

                    double latitude = bdLocation.getLatitude();
                    double longitude = bdLocation.getLongitude();
                    String address = bdLocation.getAddrStr();
                    double altitude= bdLocation.getAltitude();
                    currentPosition.set(latitude,longitude,altitude);
                    //just init location  once
                    bdLocation.setLatitude(bdLocation.getLatitude()-0.000176);
                    bdLocation.setLongitude(bdLocation.getLongitude()-0.005414);
                    startPositionView(bdLocation,mapType);
                    client.stop();

                    // Toast.makeText(getApplicationContext(),address+"\n经度："+longitude +"\n纬度:"+latitude,Toast.LENGTH_LONG);
                }
                }
            };
        client.setLocOption(mOption);
        client.registerLocationListener(locationListener);
        client.start();
        return currentPosition;
    }

    public void startPositionView(BDLocation amapLocation,int type){
        Globe globe = getWorldWindow().getGlobe();
        Camera camera = new Camera();
        if(type==2) {
            //2D
            Position eye = new Position(35, 121.527, 1e3);
            if(amapLocation!=null) {
                eye = new Position(amapLocation.getLatitude(),amapLocation.getLongitude(), 1e3);
            }
            camera.set(eye.latitude, eye.longitude, eye.altitude, WorldWind.ABSOLUTE, 0, 0, 0.0 /*roll*/);
        }else if(type==3){
            //3D
            Position eye = new Position(35, 121.527, 40000);
            if(amapLocation!=null) {
                eye = new Position(amapLocation.getLatitude(),amapLocation.getLongitude(), 40000);
            }
            camera.set(eye.latitude, eye.longitude, eye.altitude, WorldWind.ABSOLUTE, 0, 80, 0.0 /*roll*/);
        }
        getWorldWindow().getNavigator().setAsCamera(globe, camera);
    }

    public void  traceCurrentLocation(){

  /*  try{
        LocationManager lm;
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        *//*Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        LocationProvider lp = lm.getProvider(LocationManager.GPS_PROVIDER);*//*
        if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                           *//* Toast.makeText(getApplicationContext(),"NetWork abled",Toast.LENGTH_LONG).show();*//*
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocListener());
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                                  *//*  Toast.makeText(getApplicationContext(), "update location", Toast.LENGTH_LONG).show();*//*
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocListener());
            } else {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();
                Position position = new Position(latitude,longitude,altitude);
                drawCurrentLocation(position);
                String str  = "纬度："+latitude+"\n经度： " +longitude;
                Toast.makeText(getActivity().getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity().getApplicationContext(),"NetWork Unabled",Toast.LENGTH_SHORT).show();
        }
    }catch (SecurityException e){
        e.printStackTrace();
    }*/
        //gaode location
       /* final AMapLocationClient mLocationClient  = new AMapLocationClient(getActivity().getApplicationContext());
        //声明定位回调监听器
        final AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {

                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //可在其中解析amapLocation获取相应内容。
                       double latitude = amapLocation.getLatitude();
                       double longitude = amapLocation.getLongitude();
                       String address = amapLocation.getAddress();
                       double altitude= amapLocation.getAltitude();
                        //just init location  once
                        amapLocation.setLongitude(amapLocation.getLongitude()-0.0054);
                        drawCurrentLocation(amapLocation);
                        // Toast.makeText(getApplicationContext(),address+"\n经度："+longitude +"\n纬度:"+latitude,Toast.LENGTH_LONG);
                    }
                }
            }
        };

        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
         mLocationOption.setInterval(2000);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
*/
        //***baidu location

        final LocationClient client = new LocationClient(getActivity().getApplicationContext());
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
                    //0.000176=35.941618-35.941442,0.005414=120.171533-120.166119
                    bdLocation.setLatitude(bdLocation.getLatitude()-0.000176);
                    bdLocation.setLongitude(bdLocation.getLongitude()-0.005414);
                    drawCurrentLocation(bdLocation);
                }
            }
        };
        client.setLocOption(mOption);
        client.registerLocationListener(locationListener);
        client.start();
    }

    public void drawCurrentLocation(BDLocation amapLocation){

        LayerList layers =wwd.getLayers();
        Layer currentLocationLayer = null;
        for(Layer layer : layers){
            if(layer.getDisplayName().equals("currentLocationMark")){
                currentLocationLayer=layer;
                break;
            }
        }
        if(currentLocationLayer!=null) {
            wwd.getLayers().removeLayer(currentLocationLayer);
        }
        // Create a RenderableLayer for placemarks and add it to the WorldWindow
        RenderableLayer placemarksLayer = new RenderableLayer("currentLocationMark");
        wwd.getLayers().addLayer(placemarksLayer);

        //////////////////////////////////////
        // Second, create some placemarks...
        /////////////////////////////////////

        // Create a simple placemark at downtown Ventura, CA. This placemark is a 20x20 cyan square centered on the
        // geographic position. This placemark demonstrates the creation with a convenient factory method.
        Placemark ventura = Placemark.createWithColorAndSize(Position.fromDegrees(amapLocation.getLatitude(), amapLocation.getLongitude(), amapLocation.getAltitude()), new Color(0, 0, 1, 1), 40);

        placemarksLayer.addRenderable(ventura);
    }

  /*  protected void positionView3D(WorldWindow wwd) {

        Position mtRainier = new Position(35.852886, 121.760374, 4392.0);
        Position eye = new Position(35.912, 121.527, 40000.0);

        // Compute heading and distance from peak to eye
        Globe globe = wwd.getGlobe();
        double heading = eye.greatCircleAzimuth(mtRainier);
        double distanceRadians = mtRainier.greatCircleDistance(eye);
        double distance = distanceRadians * globe.getRadiusAt(mtRainier.latitude, mtRainier.longitude);

        // Compute camera settings
        double altitude = eye.altitude - mtRainier.altitude;
        double range = Math.sqrt(altitude * altitude + distance * distance);
        double tilt = Math.toDegrees(Math.atan(distance / eye.altitude));

        // Apply the new view
        Camera camera = new Camera();
        camera.set(eye.latitude, eye.longitude, eye.altitude, WorldWind.ABSOLUTE, 0, 80, 0.0 *//*roll*//*);

        wwd.getNavigator().setAsCamera(globe, camera);
    }

    protected void positionViewOnlineMap(WorldWindow wwd) {

        Position mtRainier = new Position(35.852886, 121.760374, 4392.0);
        Position eye = new Position(35.912, 121.527, 1e7);

        // Compute heading and distance from peak to eye
        Globe globe = wwd.getGlobe();
        double heading = eye.greatCircleAzimuth(mtRainier);
        double distanceRadians = mtRainier.greatCircleDistance(eye);
        double distance = distanceRadians * globe.getRadiusAt(mtRainier.latitude, mtRainier.longitude);

        // Compute camera settings
        double altitude = eye.altitude - mtRainier.altitude;
        double range = Math.sqrt(altitude * altitude + distance * distance);
        double tilt = Math.toDegrees(Math.atan(distance / eye.altitude));

        // Apply the new view
        Camera camera = new Camera();
        camera.set(eye.latitude, eye.longitude, eye.altitude, WorldWind.ABSOLUTE, 0, 0, 0.0 *//*roll*//*);

        wwd.getNavigator().setAsCamera(globe, camera);
    }*/


    public class LocListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            String str = "当前位置:\n";
            double latitude;
            double longitude;
            double altitude;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            //drawCurrentLocation(new Position(latitude,longitude,altitude));
            str  = str+"纬度："+latitude+"\n经度： " +longitude;
            Toast.makeText(getActivity().getApplicationContext(),str,Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getActivity().getApplicationContext(),"状态改变！",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getActivity().getApplicationContext(),"位置提供者被启用！",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getActivity().getApplicationContext(),"位置提供者被禁用！",Toast.LENGTH_SHORT).show();
        }
    }

}
