package com.Lokos.moscowinstitutionssearch;

import java.util.ArrayList;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class StatisticsActivity extends Activity {

    ArrayList<MyAdapter.Item> items = new ArrayList<MyAdapter.Item>();
    MyAdapter myAdapter;
    private SQLiteDatabase db;
    private DBHelper myDBHelper;
    private String name;
    private int counter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_layout);

        myDBHelper= new DBHelper(this);

        fillData();
        myAdapter = new MyAdapter(this, items);

        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.lv_statistics);
        lvMain.setAdapter(myAdapter);
    }

    // генерируем данные для адаптера
    void fillData() {
        db = myDBHelper.getWritableDatabase();
        Cursor cursor_statistics = myDBHelper.decreasing_order();
        if (cursor_statistics.getCount() > 0) {
            while (cursor_statistics.moveToNext()) {
                counter= Integer.parseInt(cursor_statistics.getString(4));
                name= cursor_statistics.getString(1);
                Log.d("TESTING","name= "+name);
                Log.d("TESTING","counter= "+counter);
                items.add(new MyAdapter.Item(counter, name));
            }
        }
    }
}