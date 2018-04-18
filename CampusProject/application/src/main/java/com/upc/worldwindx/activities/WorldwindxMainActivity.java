package com.upc.worldwindx.activities;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.upc.R;
import com.upc.spatialite.utilities.ActivityHelper;
import com.upc.spatialite.utilities.AssetHelper;
import com.upc.worldwindx.fragments.BasicGlobeFragment;
import com.upc.worldwindx.fragments.TileSurfaceImageFragment;
import com.upc.spatialite.service.SpatialiteService;
import com.upc.worldwindx.utilities.GeometryFromText;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.shape.ShapeAttributes;
import gov.nasa.worldwind.shape.TextAttributes;
import jsqlite.Constants;
import jsqlite.Stmt;

/**
 * Created by Lenovo on 2018/3/28.
 */


public class WorldwindxMainActivity extends AppCompatActivity implements View.OnClickListener{

    // a button for recording data to local DB
    Button btnRecord = null;
    BasicGlobeFragment globlefragment = null;
    EditText searchPOI = null;
    ListView listPOI = null;
    // 读写锁，解决轨迹记录时实时数据库读写
    static ReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * 获取布局控件，加载本地数据库和地球视图
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worldwindx_main_layout);
        btnRecord = (Button)findViewById(R.id.btnRecord);
        searchPOI = (EditText)findViewById(R.id.SearchPOI);
        listPOI = (ListView)findViewById(R.id.listPOI);
        //step 1 :load DB by copying local_db.sqlite into a directory
         loadLocalDB();
        //step 2 :load Fragment to show globle and layer
          if(savedInstanceState==null){
            globlefragment = loadTutorial(TileSurfaceImageFragment.class,R.string.app_name);
          }
    }

    /**
     * 加载特定的地图视图Fraggment
     * @param globeFragment
     * @param titleId
     * @return
     */
    protected BasicGlobeFragment loadTutorial(Class<? extends BasicGlobeFragment> globeFragment,int titleId) {
        try {
            this.setTitle(titleId);
            BasicGlobeFragment globe = globeFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.globe_container, globe)    // replace (destroy) existing fragment (if any)
                    .commit();
            return globe;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加载本地数据库，将数据库文件复制到指定路径
     * */
    protected  void loadLocalDB(){
        try {
            //step 1:check and copy dbfile
            File dbFile = new File( this.getExternalFilesDir(null).toString()+"/"+getString(R.string.local_db));
            if(!dbFile.exists()) {
                ActivityHelper.showAlert(this,
                        "dbFile_location:"+this.getExternalFilesDir(null).toString());
                AssetHelper.CopyAsset(this,
                        ActivityHelper.getPath(this, true),
                        getString(R.string.local_db));
            }
            //step 2:create necessary table
            try {
                SpatialiteService.CreateSpatialTableWithIndex(getApplicationContext());
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch(IOException e){
            ActivityHelper.showAlert(this,"Failed to copy db to external");
        }
    }

    /**
     * searchPOI:输入搜索兴趣点名称文本框事件监听
     * listPOI:相关POI列表项单击选中事件监听
     * */
    @Override
    protected void onStart() {
        super.onStart();
        searchPOI.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //select from  poi table to create listItems
                try {
                    String poiName  = searchPOI.getText().toString();
                    String sql ="SELECT name FROM test_geom WHERE name LIKE  '%"+poiName+"%'";
                    SpatialiteService.connectlocalDB(getApplicationContext(), Constants.SQLITE_OPEN_READWRITE);
                    Stmt stmt = SpatialiteService.getDb().prepare(sql);
		/*	Stmt stmt = db
					.prepare("SELECT fid,name,type,description,ST_AsText(the_geom) FROM test_geom;");*/
		            List<String> poiAttr = new ArrayList<>();
                    while (stmt.step()) {
                         poiAttr.add(stmt.column_string(0));
                    }
                    SpatialiteService.getDb().close();
                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.array_item,poiAttr);
                    listPOI.setAdapter(listAdapter);
                    listPOI.setVisibility(View.VISIBLE);
                } catch (jsqlite.Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listPOI.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchPOI.setText(listPOI.getItemAtPosition(position).toString());
                listPOI.setVisibility(View.INVISIBLE);
            }
        });

    }
/** to listener click events
 * btnRecord :记录位置按钮
 * btnRouteRecord :记录轨迹按钮
 * btnAddMark:添加注记
 * btnSearch：兴趣点搜索按钮
 * */
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnRecord){
        Intent recordLocaldata = new Intent(this,RecordLocalDataActivity.class);
        startActivity(recordLocaldata);
        }else if(v.getId()==R.id.btnRouteRecord){
            //create table
            final String  tablename ="route";
            //intit location module
            /*   final AMapLocationClient  mLocationClient  = new AMapLocationClient(this);
            //声明定位回调监听器
            AMapLocationListener   mLocationListener = new AMapLocationListener() {
                @Override
                public void onLocationChanged(final AMapLocation amapLocation) {
                    if (amapLocation != null) {
                        if (amapLocation.getErrorCode() == 0) {
                            //可在其中解析amapLocation获取相应内容。

                            //存入数据
                            new Thread() {
                                public void run() {
                                    try {
                                        rwl.writeLock().lock();
                                        String description = amapLocation.getAddress();
                                        String geometry = "POINTZ(" + amapLocation.getLatitude() + " " + amapLocation.getLongitude() +  " " + amapLocation.getAltitude()+")";
                                        boolean insertSuccess = SpatialiteService.insertTableForRoute(getApplicationContext(), tablename, description, geometry);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    } finally{
                                        rwl.writeLock().unlock();
                                     }
                                }
                            }.start();

                            // 绘制轨迹
                           new Thread(){
                           public void run(){
                               try {
                                   rwl.readLock().lock();
                                   drawRoute(tablename);
                               }catch (Exception e){
                                   e.printStackTrace();
                               }finally {
                                   rwl.readLock().unlock();
                               }
                           }}.start();

                        }
                    }
                }
            };
            //设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms
             mLocationOption.setInterval(5000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);*/

 /**baidu location
*/
          //step 1 :set location funcation

           final LocationClient client = new LocationClient(getApplicationContext());
            LocationClientOption mOption = new LocationClientOption();
            mOption.setScanSpan(2000);
            mOption.setOpenGps(true);
            mOption.setIsNeedAltitude(true);
            mOption.setIsNeedAddress(true);
            BDAbstractLocationListener locationListener = new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(final BDLocation bdLocation) {
                    if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                        //可在其中解析amapLocation获取相应内容。
                        bdLocation.setLatitude(bdLocation.getLatitude()-0.000176);
                        bdLocation.setLongitude(bdLocation.getLongitude()-0.005414);
                        //存入数据
                        new Thread() {
                            public void run() {
                                try {
                                    rwl.writeLock().lock();
                                    String description = bdLocation.getAddrStr();
                                    double latitude = bdLocation.getLatitude();
                                    double longitude =bdLocation.getLongitude();
                                    double altitude = bdLocation.getAltitude();
                                    BigDecimal bigDecimal = new BigDecimal(altitude);
                                    String altitudeString  =bigDecimal.toPlainString();
                                    altitude = Double.parseDouble(altitudeString.substring(0,altitudeString.indexOf(".")+3));
                                    String geometry = "POINTZ(" + longitude + " " + latitude +  " " +altitude+")";
                                    boolean insertSuccess = SpatialiteService.insertTableForRoute(getApplicationContext(), tablename, description, geometry);
                                }catch (Exception e){
                                    e.printStackTrace();
                                } finally{
                                    rwl.writeLock().unlock();
                                }
                            }
                        }.start();

                        // 绘制轨迹
                        new Thread(){
                            public void run(){
                                try {
                                    rwl.readLock().lock();
                                    drawRoute(tablename);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }finally {
                                    rwl.readLock().unlock();
                                }
                            }}.start();
                    }
                }
            };
            client.setLocOption(mOption);
            client.registerLocationListener(locationListener);
            //step 2 :Menu design
            PopupMenu  popupMenu = new PopupMenu(this,v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                          //开始记录轨迹
                        case R.id.startRoute:
                            //step 1:create table to store route data
                           boolean createSuccess = SpatialiteService.createTableForRoute(getApplicationContext(),tablename);
                            //step 2:start location and draw route
                            if(createSuccess) {
                                client.start();
                                Toast.makeText(getApplicationContext(), "开始记录轨迹！", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "请删除已有轨迹！", Toast.LENGTH_SHORT).show();
                            }
                               return true;
                            //结束记录轨迹
                        case R.id.endRoute:
                            //stop locaiton
                             client.stop();
                            Toast.makeText(getApplicationContext(),"结束记录轨迹！",Toast.LENGTH_SHORT).show();
                            return true;
                            //删除轨迹
                        case R.id.deleteRoute:
                            boolean dropSuccess = SpatialiteService.dropTableForRoute(getApplicationContext(), tablename);
                            if (dropSuccess) {
                                //remove routeLayer if still exist
                                WorldWindow wwd =globlefragment.getWorldWindow();
                                LayerList layers =wwd.getLayers();
                                Layer routeLayer = null;
                                for(Layer layer : layers){
                                    if(layer.getDisplayName().equals("routeLayer")){
                                        routeLayer=layer;
                                        break;
                                    }
                                }
                                if(routeLayer!=null) {
                                    wwd.getLayers().removeLayer(routeLayer);
                                }
                                //show remind information
                                Toast.makeText(getApplicationContext(), "轨迹记录已删除！", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });

            inflater.inflate(R.menu.route_popupmenu,popupMenu.getMenu());
            popupMenu.show();

        }else if(v.getId()==R.id.btnAddMark){

            //uss navigator to locate a place and add mark on it

            Intent addPlaceMark = new Intent(this,AddPlacemarkActivity.class);
            startActivity(addPlaceMark);

        }else if(v.getId()==R.id.btnSearch){
            //step 1 select geometry from table test_geom
            try {
                String poiName = searchPOI.getText().toString();
                String sql = "SELECT description , ST_AsText(the_geom) FROM test_geom WHERE name LIKE  '%" + poiName + "%'";
                SpatialiteService.connectlocalDB(getApplicationContext(), Constants.SQLITE_OPEN_READWRITE);
                Stmt stmt = SpatialiteService.getDb().prepare(sql);
		        /*	Stmt stmt = db
					.prepare("SELECT fid,name,type,description,ST_AsText(the_geom) FROM test_geom;");*/
                List<String> poiGeom = new ArrayList<>();
                List<String> poiDescrip = new ArrayList<>();
                while (stmt.step()) {
                    poiDescrip.add(stmt.column_string(0));
                    poiGeom.add(stmt.column_string(1));
                }
                SpatialiteService.getDb().close();
                //step 2 to draw pois
                drawPOI(poiGeom,poiDescrip);
            }catch (jsqlite.Exception e){
                e.printStackTrace();
            }
        }
    }

/** draw route */
    public void drawRoute(String tablename){
        //step 1:read data from database route table
        String selectSql = "SELECT ST_AsText(the_geom) FROM "+tablename;
        List<String> routeData = SpatialiteService.selectTableForRoute(getApplicationContext(),selectSql);
      if(routeData!=null) {
         // Toast.makeText(getApplicationContext(),"routeData:"+routeData.toString(),Toast.LENGTH_SHORT).show();
          Double[][] routePoints = new Double[routeData.size()][3];//pointsnumber (x,y,z)
          int i = 0;
          Iterator iterator = routeData.iterator();
          while (iterator.hasNext()) {
              routePoints[i] = GeometryFromText.pointZFromText(iterator.next().toString());
              i++;
          }
          //step 2:create renderables and add RenderableLayer to wwd
          WorldWindow wwd =globlefragment.getWorldWindow();
          //remove routeLayer if already exist
          LayerList layers =wwd.getLayers();
          Layer routeLayer = null;
          for(Layer layer : layers){
              if(layer.getDisplayName().equals("routeLayer")){
                  routeLayer=layer;
                  break;
              }
          }
              if(routeLayer!=null) {
              wwd.getLayers().removeLayer(routeLayer);
          }
          RenderableLayer renderableLayer = new RenderableLayer("routeLayer");
          List<Position> positions = new ArrayList<>();
       //  positions.add(Position.fromDegrees(35.95, 120.18, 10));
          for (int j = 0; j < routePoints.length; j++) {
              positions.add(Position.fromDegrees(routePoints[j][1], routePoints[j][0], 10));
          }
          ShapeAttributes attrs = new ShapeAttributes();
          attrs.setDrawVerticals(true); // display the extruded verticals
          attrs.setInteriorColor(new Color(1, 1, 1, 0)); // 50% transparent white
          attrs.setOutlineWidth(10);
          List<Position> test =new ArrayList<>();
          Path path = new Path(positions, attrs);
          renderableLayer.addRenderable(path);
          wwd.getLayers().addLayer(renderableLayer);
      }
    }

    /**to draw selected pois
    */
    public void drawPOI(List poiList,List poiDescrip){
           //step 1 :to convert geometry text to  position data
        try{
        if(poiList!=null) {
            // Toast.makeText(getApplicationContext(),"routeData:"+routeData.toString(),Toast.LENGTH_SHORT).show();
            Double[][] routePoints = new Double[poiList.size()][2];//pointsnumber (x,y)
            int i = 0;
            Iterator iterator = poiList.iterator();
            while (iterator.hasNext()) {
                routePoints[i] = GeometryFromText.pointFromText(iterator.next().toString());
                i++;
            }
            //step 2:create renderables and add RenderableLayer to wwd
            WorldWindow wwd =globlefragment.getWorldWindow();
            //remove routeLayer if already exist
            LayerList layers =wwd.getLayers();
            Layer poiLayer = null;
            for(Layer layer : layers){
                if(layer.getDisplayName().equals("poiLayer")){
                    poiLayer=layer;
                    break;
                }
            }
            if(poiLayer!=null) {
                wwd.getLayers().removeLayer(poiLayer);
            }
            RenderableLayer renderableLayer = new RenderableLayer("poiLayer");
            //  positions.add(Position.fromDegrees(35.95, 120.18, 10));
            for (int j = 0; j < routePoints.length; j++) {
                Placemark poiPlacemark = createPlacemark(Position.fromDegrees(routePoints[j][1],routePoints[j][0],10));
                renderableLayer.addRenderable(poiPlacemark);
                TextAttributes attrs = new TextAttributes();
                attrs.setTextColor(new Color(0, 1, 0, 1)); // green via r,g,b,a
                attrs.setTextOffset(Offset.bottomCenter());
                attrs.setTypeface(Typeface.DEFAULT); // system default bold typeface
                attrs.setTextSize(30); // 48 screen pixels

                renderableLayer.addRenderable( new Label(Position.fromDegrees(routePoints[j][1],routePoints[j][0],10), poiDescrip.get(j).toString(),attrs));
            }
            wwd.getLayers().addLayer(renderableLayer);
        }
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    private static Placemark createPlacemark(Position position) {
        final double NORMAL_IMAGE_SCALE = 3.0;
        final double HIGHLIGHTED_IMAGE_SCALE = 4.0;
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.school));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(NORMAL_IMAGE_SCALE).setDrawLeader(true);
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(HIGHLIGHTED_IMAGE_SCALE));
        return placemark;
    }
}
