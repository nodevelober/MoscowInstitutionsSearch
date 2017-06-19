package com.Lokos.moscowinstitutionssearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener{
    ArrayList<ItemObject> checked_list= new ArrayList<ItemObject>();
    EditText ed, ed_search;
    LatLng latLng;
    Button button;
    RecyclerView recyclerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.filter_activty);
        ArrayList<String> category_list = (ArrayList<String>) getIntent().getSerializableExtra("category_list");

        checked_list = new ArrayList<ItemObject>();

        for (int i = 0; i < category_list.size(); i++){
            checked_list.add(new ItemObject(category_list.get(i)));
        }

        latLng= getIntent().getParcelableExtra("latLng");
        Log.d("TESTING","latLng_intent 1= "+latLng);


        ed_search = (EditText)findViewById(R.id.ed_search);
        ed = (EditText)findViewById(R.id.ed);

        button = (Button)findViewById(R.id.btn);
        button.setOnClickListener(this);
        recyclerView = (RecyclerView)findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        CategoryAdapter categoryAdapter = new CategoryAdapter();
        recyclerView.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();


    }

    @Override
    public void onClick(View v) {
        ArrayList<String> mList = new ArrayList<String>();

        for (int i = 0; i < checked_list.size(); i++){
            if (checked_list.get(i).isCheck){
                mList.add(checked_list.get(i).name);
            }
        }
        if(!ed_search.getText().toString().matches("")){
            mList.add(ed_search.getText().toString());
        }
        Intent intent= new Intent();

        Log.d("aaaaaaaaaa", "mList.size() : " + mList.size());

            intent.putExtra("checked_list",mList);

        intent.putExtra("latLng",latLng);
        if(ed.getText().toString().matches("") && ed.getText().toString().trim().length() == 0){
            intent.putExtra("radius",5000);
        }
        else{
            Log.d("TESTING","Integer.parseInt(ed.getText().toString())= "+Integer.parseInt(ed.getText().toString()));
            intent.putExtra("radius",Integer.parseInt(ed.getText().toString()));
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {


    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, null, false));
    }

    @Override
    public void onBindViewHolder(CategoryHolder holder, int position) {
        holder.categoryName.setText(checked_list.get(position).name);
        holder.checket.setChecked(checked_list.get(position).isCheck);
        holder.checket.setTag(position);
    }

    @Override
    public int getItemCount() {
        return checked_list.size();
    }

    protected class CategoryHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        TextView categoryName;
        CheckBox checket;

        public CategoryHolder(View itemView) {
            super(itemView);

            categoryName = (TextView)itemView.findViewById(R.id.text);
            checket = (CheckBox)itemView.findViewById(R.id.checkbox);
            checket.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int tag = (int)v.getTag();

            if (checked_list.get(tag).isCheck){
                checked_list.get(tag).isCheck = false;
            }else{
                checked_list.get(tag).isCheck = true;
            }

        }
    }


    }

private class ItemObject{

    String name;
    boolean isCheck = false;

    public ItemObject(String name) {
        this.name = name;
    }


}
}