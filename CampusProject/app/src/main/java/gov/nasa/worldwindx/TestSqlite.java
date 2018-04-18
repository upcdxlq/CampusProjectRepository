/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import  android.widget.Button;


/**
 * Created by Lenovo on 2018/3/20.
 */

public class TestSqlite extends Activity{

    private SQLiteDatabase mydb = null;
    private final static String DATABASE_NAME ="FirstDatabase.db";
    private final static String TABLE_NAME ="firstTable";
    private final static String ID ="_id";
    private final static String NAME ="name";
    private final static String AGE= "age";
    private final static String HOME ="home";
    private final static String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ("+ ID+" INTERGER PRIMARY KEY,"+NAME+" TEXT,"+AGE+" TEXT,"+HOME+" TEXT)";
    private EditText editText = null;
    private EditText editText1 = null;
    private  EditText editText2 = null;
    private EditText editText3 = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button btn1 = (Button)findViewById(R.id.button1);
        editText=(EditText) findViewById(R.id.editText);
        editText1=(EditText) findViewById(R.id.editText1);
        editText2=(EditText) findViewById(R.id.editText2);
        editText3=(EditText) findViewById(R.id.editText3);
        editText1.setText("");
        editText2.setText("");
        editText3.setText("");

        mydb = openOrCreateDatabase(DATABASE_NAME,MODE_PRIVATE,null);
        try {
            mydb.execSQL(CREATE_TABLE);
        }catch (Exception e){

        }

        ContentValues cv = new ContentValues();
        cv.put(NAME,"张三");
        cv.put(AGE,"18");
        cv.put(HOME,"北京");
        mydb.insert(TABLE_NAME,null,cv);

        showData();
        mydb.close();

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mydb = openOrCreateDatabase(DATABASE_NAME,MODE_PRIVATE,null);
                ContentValues cv = new ContentValues();
                cv.put(NAME,editText1.getText().toString());
                cv.put(AGE,editText2.getText().toString());
                cv.put(HOME,editText3.getText().toString());
                mydb.insert(TABLE_NAME,null,cv);
                showData();
                mydb.close();
            }
        });

    }

    public void showData(){
        editText.setText("数据库内容：\n");
        editText.append("姓名\t\t年龄\t\t籍贯\n");
        Cursor cur = mydb.query(TABLE_NAME,new String[] {ID,NAME,AGE,HOME},null,null,null,null,null);
        int count = cur.getCount();
        if(cur !=null&& count>=0){
            if(cur.moveToFirst()){
                do {
                    String name = cur.getString(1);
                    String age = cur.getString(2);
                    String home = cur.getString(3);
                    editText.append(""+name+"\t\t"+age+"\t\t"+home+"\n");

                }while(cur.moveToNext());
            }
        }
    }
}
