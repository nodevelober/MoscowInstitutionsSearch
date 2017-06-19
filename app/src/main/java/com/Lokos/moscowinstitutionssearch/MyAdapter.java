package com.Lokos.moscowinstitutionssearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by artur on 15-Jun-17.
 */

public class MyAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Item> items;

    MyAdapter(Context context, ArrayList<Item> items) {
        ctx = context;
        this.items = items;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return items.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.statictica_v, parent, false);
        }

        Item p = getProduct(position);

        // заполняем View в пункте списка данными
        ((TextView) view.findViewById(R.id.tv_count)).setText(p.counter+" ");
        ((TextView) view.findViewById(R.id.tv_name)).setText(p.name);

        return view;
    }

    // по позиции
    Item getProduct(int position) {
        return ((Item) getItem(position));
    }

    public static class Item {

        int counter;
        String name;

        Item(int counter, String name) {
            this.counter = counter;
            this.name = name;
        }
    }

}