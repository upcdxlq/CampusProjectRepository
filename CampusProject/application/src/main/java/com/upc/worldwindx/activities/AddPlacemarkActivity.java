package com.upc.worldwindx.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.upc.R;
import com.upc.spatialite.service.SpatialiteService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;

/**
 * Created by Lenovo on 2018/4/10.
 */

public class AddPlacemarkActivity extends Activity{
    // Use pre-allocated navigator state objects to avoid per-event memory allocations
    private LookAt lookAt = new LookAt();
    private Camera camera = new Camera();
    // Track the navigation event time so the overlay refresh rate can be throttled
    private long lastEventTime;

    EditText txtlatitude = null;
    EditText txtlongitude =null;
    EditText txtname = null;
    EditText txttype = null;
    EditText txtdescription = null;
    Button btnAdd = null;
    Button btnPublic = null;
    TextView txtResult = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addplacemark_layout);
        txtlatitude = (EditText)findViewById(R.id.markLatitude);
        txtlongitude = (EditText)findViewById(R.id.markLongitude);
        txtname = (EditText)findViewById(R.id.markName);
        txttype = (EditText)findViewById(R.id.markType);
        txtdescription = (EditText)findViewById(R.id.markDescription);
        btnAdd = (Button)findViewById(R.id.btnAdd);
        btnPublic = (Button)findViewById(R.id.btnPublic);
        txtResult = (TextView)findViewById(R.id.markResult);

        Bundle bundle = getIntent().getBundleExtra("arguments");
        double latitude = bundle.getDouble("latitude");
        double longitude = bundle.getDouble("longitude");
        txtlatitude.setText(String.valueOf(latitude));
        txtlongitude.setText(String.valueOf(longitude));

    }

    @Override
    protected void onStart() {
        super.onStart();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        btnPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启线程来发起网络请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                                    try {

                                    String name = txtname.getText().toString();
                                    String type = txttype.getText().toString();
                                    String description = txtdescription.getText().toString();
                                    String latitude = txtlatitude.getText().toString();
                                    String longitude = txtlongitude.getText().toString();
                                    name = URLEncoder.encode(name,"utf-8");
                                    type = URLEncoder.encode(type,"utf-8");
                                    description = URLEncoder.encode(description,"utf-8");
                                    //  String geometry = "POINT("+Double.parseDouble(txtlongitude.getText().toString())+" "+Double.parseDouble(txtlatitude.getText().toString())+")";
                                    //1.获取到HttpConnection的实例，new出一个URL对象，并传入目标的网址，然后调用一下openConnection（）方法。
                                    HttpURLConnection connection = null;
                                    String urlString = "http://172.26.104.241:8080/RemoteSpatialite/VisitSpatialiteToInsert?name=" + name + "&type=" + type + "&description=" + description + "&latitude=" + latitude + "&longitude=" + longitude;

                                    URL url = new URL(urlString);
                                    connection = (HttpURLConnection) url.openConnection();
                                    //2.得到了HttpConnection的实例后，设置请求所用的方法（GET：从服务器获取数据，POST：提交数据给服务器）
                                    connection.setRequestMethod("GET");
                                    connection.setRequestProperty("Accept-Charset", "utf-8");
                                    //3.自由定制的环节（设置连接超时，读取的毫秒数，以及服务器希望得到的消息头等）
                                    connection.setConnectTimeout(8000);
                                    connection.setReadTimeout(8000);
                                   // connection.setRequestProperty("Content-Type", "utf-8");
                                    //4.利用getInputStream（）方法获取服务器的返回的输入流，然后读取
                                    InputStream in = connection.getInputStream();
                                    //下面对获取到的输入流进行读取
                                    BufferedReader bufr = new BufferedReader(new InputStreamReader(in));
                                    StringBuilder response = new StringBuilder();
                                    String line = null;
                                    while ((line = bufr.readLine()) != null) {
                                        response.append(line);
                                        try {
                                            //断开已有连接
                                            connection.disconnect();
                                            //建立新的连接
                                            connection = null;
                                            urlString = "http://172.26.104.241:8080/RemoteSpatialite/VisitSpatialiteToSelect?name=" + name;
                                            url = new URL(urlString);
                                            connection = (HttpURLConnection) url.openConnection();
                                            connection.setRequestMethod("GET");
                                            connection.setRequestProperty("Accept-Charset", "utf-8");
                                            InputStream is = connection.getInputStream();
                                            //下面对获取到的输入流进行读取
                                            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                                            String lineString = null;
                                            while ((lineString = buf.readLine()) != null) {
                                                response.append(lineString);
                                            }
                                            txtResult.setText(response);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    //5.调用disconnect（）方法将HTTP连接关闭掉
                                    if (connection != null) {
                                        connection.disconnect();
                                    }

                                }catch(IOException e)
                                {
                                    e.printStackTrace();
                                }
                    }
                }).start();
            }
        });
    }
}
