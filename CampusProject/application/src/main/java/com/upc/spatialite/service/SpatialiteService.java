package com.upc.spatialite.service;

import android.app.ListActivity;
import android.content.Context;
import android.util.Log;

import com.upc.R;
import com.upc.spatialite.utilities.ActivityHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jsqlite.Callback;
import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Stmt;

import  android.content.Context;


/**
 * Created by Lenovo on 2018/4/5.
 */

/**
 * local_db.sqlite
 *
 * Create>>
 CREATE TABLE poiSet(
 pid integer not null primary key,
 name text not null,
 type text not null,
 description text
 )
 add geometry >>
 SELECT AddGeometryColumn('poiSet','geometry',4326,'POINT','XY')

 */
public class SpatialiteService {
    private static  final String TAG = "InsertDB";
    private static jsqlite.Database db = new jsqlite.Database();

    public static Database getDb() {
        return db;
    }
    public static void   connectlocalDB(Context context,int openmode){
        try {
            String dbFile = ActivityHelper.getDataBase(context,
                    context.getString(R.string.local_db));
            if (dbFile == null) {
                ActivityHelper.showAlert(context,
                        context.getString(R.string.error_locate_failed));
                throw new IOException(context.getString(R.string.error_locate_failed));
            }
            db.open(dbFile.toString(), openmode);

        } catch (jsqlite.Exception e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    public static int getRecordsNumber(Context context,String sql) {
        int count = 0;
        try {
            connectlocalDB(context,Constants.SQLITE_OPEN_READWRITE);
            Stmt stmt = db.prepare(sql);
		/*	Stmt stmt = db
					.prepare("SELECT f_table_name, type, srid FROM geometry_columns;");*/
            while (stmt.step()) {
                count = stmt.column_int(0);
            }
            db.close();
            return count;
        } catch (jsqlite.Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }
    public static boolean insertLocalDB(Context context,String name,String type ,String description,String geometry){

        try {
            connectlocalDB(context,Constants.SQLITE_OPEN_READWRITE|Constants.SQLITE_OPEN_CREATE);

            Stmt stmt01 = db
                    .prepare("INSERT INTO test_geom (name, type, description, the_geom) VALUES (?,?,?,GeomFromText(?, 4326));");
            stmt01.bind(1, name);
            stmt01.bind(2, type);
            stmt01.bind(3,description);
            stmt01.bind(4,geometry);
            stmt01.step();

            stmt01.clear_bindings();
            stmt01.reset();
            stmt01.close();
            db.close();
            return  true;
        } catch (jsqlite.Exception e) {
            Log.e(TAG, e.getMessage());
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private static void insertSpatialData() throws Exception {
        Stmt stmt01 = db
                .prepare("INSERT INTO test_geom (name, measured_value, the_geom) VALUES (?,?,GeomFromText(?, 4326));");
        stmt01.bind(1, "first point");
        stmt01.bind(2, 1.23456);
        stmt01.bind(3, "POINT(1.01 2.02)");
        stmt01.step();

        stmt01.clear_bindings();
        stmt01.reset();

        stmt01.bind(1, "second point");
        stmt01.bind(2, 2.34567);
        stmt01.bind(3, "POINT(2.02 3.03)");
        stmt01.step();

        stmt01.clear_bindings();
        stmt01.reset();

        stmt01.bind(1, "third point");
        stmt01.bind(2, 3.45678);
        stmt01.bind(3, "POINT(3.03 4.04)");
        stmt01.step();

        stmt01.close();
    }

    public static void CreateSpatialTableWithIndex(Context context) throws Exception {

        connectlocalDB(context,Constants.SQLITE_OPEN_READWRITE|Constants.SQLITE_OPEN_CREATE);

        db.spatialite_create();

        db.exec("CREATE TABLE test_geom (fid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, description TEXT);",
                null);

        db.exec("SELECT AddGeometryColumn('test_geom', 'the_geom', 4326, 'POINT', 'XY');",
                null);

        db.exec("SELECT CreateSpatialIndex('test_geom', 'the_geom');", null);
    }

    public static String SelectData(Context context,String sql){

        try {
            String result ="";
            connectlocalDB(context,Constants.SQLITE_OPEN_READWRITE);
            Stmt stmt = db.prepare(sql);
		/*	Stmt stmt = db
					.prepare("SELECT fid,name,type,description,ST_AsText(the_geom) FROM test_geom;");*/
            while (stmt.step()) {
                result += stmt.column_string(0)+",";
                result += stmt.column_string(1)+",";
                result += stmt.column_string(2)+",";
                result += stmt.column_string(3)+",";
                result += stmt.column_string(4)+"\n";
            }
            db.close();
            return result;
        } catch (jsqlite.Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    //record route service
    public static boolean createTableForRoute(Context context,String tablename){

        try {
            connectlocalDB(context, Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);

            db.spatialite_create();

            db.exec("CREATE TABLE "+tablename+" (fid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,description TEXT);",
                    null);

            db.exec("SELECT AddGeometryColumn('"+tablename+"', 'the_geom', 4326, 'POINT', 'XYZ');",
                    null);

            db.exec("SELECT CreateSpatialIndex('"+tablename+"', 'the_geom');", null);

            db.close();
            return true;
        }catch (jsqlite.Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean dropTableForRoute(Context context,String tablename){
        try {
            connectlocalDB(context, Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);

            db.exec("DROP TABLE "+ tablename +";",null);
            db.close();
            return true;
        }catch (jsqlite.Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean insertTableForRoute(Context context,String tablename,String description,String geometry){
        try {

            connectlocalDB(context, Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);

            Stmt stmt01 = db
                    .prepare("INSERT INTO "+tablename+" (description, the_geom) VALUES (?,GeomFromText(?, 4326));");
            stmt01.bind(1,description);
            stmt01.bind(2,geometry);
            stmt01.step();
            db.close();
            return true;
        }catch (jsqlite.Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static   List<String>  selectTableForRoute(Context context,String sql){
        try {
            List<String> route= new ArrayList<>();
            connectlocalDB(context, Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);

            Stmt stmt01 = db.prepare(sql);
            int i=0;
            while (stmt01.step()) {
                route.add(stmt01.column_string(0));
                i++;
            }
            db.close();
            return route;
        }catch (jsqlite.Exception e){
            e.printStackTrace();
        }
        return null;
    }





}
