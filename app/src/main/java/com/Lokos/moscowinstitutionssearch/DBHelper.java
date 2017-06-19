package com.Lokos.moscowinstitutionssearch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {
    final static String DB_NAME= "DATABASE_TEMPLATE";
    final static String TABLE_NAME= "TABLE_TEMPLATE";
    final static int TABLE_VERSION= 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, TABLE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, CommonName TEXT NOT NULL, Category TEXT NOT NULL, coordinates TEXT NOT NULL, Count INTEGER NOT NULL, Location TEXT NOT NULL, WebSite TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
    }

    public void insert(String CommonName, String Category, String coordinates, String Location, String WebSite ){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put("CommonName",CommonName);
        contentValues.put("Category",Category);
        contentValues.put("Count",0);
        contentValues.put("coordinates",coordinates);
        contentValues.put("Location",Location);
        contentValues.put("WebSite",WebSite);
        Log.d("TESTING","Location= "+Location);
        Log.d("TESTING","WebSite= "+WebSite);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public void update(int ID, int count){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put("Count",count);
        db.update(TABLE_NAME, contentValues, "ID="+ID, null);
    }

    public Cursor getFiled_by_ID(int ID_VALUE){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+TABLE_NAME+ " WHERE ID= "+ID_VALUE, null);
        return res;
    }

    public Cursor getAll(){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        return res;
    }

    public Cursor getBy_selected_categories(ArrayList<String> checked_list){
        SQLiteDatabase db= this.getWritableDatabase();
        String joined = TextUtils.join("','", checked_list);
        Log.d("TESTING","joined= "+joined);
        Log.d("TESTING","joined_query= "+"('"+joined+"')");
        Cursor res= db.rawQuery("SELECT * FROM "+TABLE_NAME+ " WHERE Category IN ('"+joined+"')", null);
        return res;
    }

    public Cursor getBy_selected_name(String name){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+TABLE_NAME+ " WHERE CommonName IN ('"+name+"')", null);
        return res;
    }



    public Cursor decreasing_order(){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+TABLE_NAME+ " WHERE Count > 0 ORDER BY Count DESC", null);
        return res;
    }

}
