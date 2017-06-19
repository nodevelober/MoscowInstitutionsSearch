package com.Lokos.moscowinstitutionssearch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private MarkerOptions markerOptions;
    private Marker marker;
    private Marker marker_target;
    public static double latitude = 0;
    public static double longitude = 0;
    private String latitude_for_insert;
    private String longitude_for_insert;
    private LatLng latLng;
    private SQLiteDatabase db;
    private DBHelper myDBHelper;
    private ArrayList<String> category_list = new ArrayList<String>();
    private ArrayList<String> checked_list;
    private ArrayList<Marker> marker_list = new ArrayList<Marker>();
    private Cursor cursor;
    private Circle circle;
    private CircleOptions circleOptions;
    private int radius= 5000;
    private float currentZoomLevel;
    private float animateZomm;
    private boolean isStartApp = false;


    private AutoCompleteTextView tv_autocomplete;
    private ArrayList<String> name_list = new ArrayList<String>();
    private String latitude_target;
    private String longitude_target;
    private TextView tv_distance_address, tv_time_email;
    private double distance, time;
    private String name_target;
    private String address, email;
    private DecimalFormat df;
    private int count_current, id_target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyApplication myApplication = (MyApplication)getApplicationContext();
        myApplication.setAppCreate(true);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        myDBHelper= new DBHelper(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Cursor cursor_for_test= myDBHelper.getAll();
        if(cursor_for_test.getCount()==0){
            new Thread(new Runnable() {
                public void run() {
                    parsingJSON();
                }
            }).start();
        }

        tv_distance_address= (TextView) findViewById(R.id.tv_distance_address);
        tv_time_email= (TextView) findViewById(R.id.tv_time_email);
        tv_autocomplete = (AutoCompleteTextView) findViewById(R.id.tv_autocomplete);

        db = myDBHelper.getWritableDatabase();
        Cursor cursor_for_names = myDBHelper.getAll();
        if (cursor_for_names.getCount() > 0) {
            while (cursor_for_names.moveToNext()) {
                String name = cursor_for_names.getString(1);
                if (!name_list.contains(name)) {
                    name_list.add(name);
                }
            }
        }
        db.close();

        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_list);
        tv_autocomplete.setAdapter(adapter);

        tv_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                db = myDBHelper.getWritableDatabase();
                name_target = (String)parent.getItemAtPosition(pos);
                Cursor cursor_for_selected_autocomplete = myDBHelper.getBy_selected_name(name_target);
                if (cursor_for_selected_autocomplete.getCount() > 0) {
                    while (cursor_for_selected_autocomplete.moveToNext()) {
                        longitude_target=cursor_for_selected_autocomplete.getString(3).substring(cursor_for_selected_autocomplete.getString(3).indexOf(",")).substring(1);
                        latitude_target=cursor_for_selected_autocomplete.getString(3).substring(0,cursor_for_selected_autocomplete.getString(3).indexOf(","));
                        if(marker_target== null){
                            marker_target= mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(latitude_target), Double.parseDouble(longitude_target)))
                                    .title(cursor_for_selected_autocomplete.getString(1)));
                        }
                        else{
                            marker_target.setPosition(new LatLng(Double.parseDouble(latitude_target), Double.parseDouble(longitude_target)));
                            marker_target.setTitle(cursor_for_selected_autocomplete.getString(1));
                        }

                        df = new DecimalFormat("#.##");
                        distance= SphericalUtil.computeDistanceBetween(latLng, new LatLng(Double.parseDouble(latitude_target), Double.parseDouble(longitude_target)));
                        time= distance/5000.0;
                        tv_distance_address.setText(df.format(distance)+" м");
                        tv_time_email.setText(df.format(time)+" ч. (пешком)");
                    }
                }
                db.close();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                if(mMap!=null){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude_target), Double.parseDouble(longitude_target)), animateZomm));
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(marker_target)) {
            db = myDBHelper.getWritableDatabase();
            Cursor cursor_for_selected_autocomplete = myDBHelper.getBy_selected_name(name_target);
            if (cursor_for_selected_autocomplete.getCount() > 0) {
                while (cursor_for_selected_autocomplete.moveToNext()) {
                    address= cursor_for_selected_autocomplete.getString(5);
                    email= cursor_for_selected_autocomplete.getString(6);
                    count_current= Integer.parseInt(cursor_for_selected_autocomplete.getString(4));

                    id_target= Integer.parseInt(cursor_for_selected_autocomplete.getString(0));
                    Log.d("TESTING","count_current= "+count_current);
                    Log.d("TESTING","id_target= "+id_target);
                    tv_distance_address.setText(address);
                    tv_time_email.setText(email);
                }
            }
            db.close();
            openDialog();
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                                ,10);
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        //noinspection MissingPermission
                        locationManager.requestLocationUpdates("gps", 5000, 0, listener);
                        //noinspection MissingPermission
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                    }
                    return;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(locationManager!=null){
            //noinspection MissingPermission
            Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation!=null && latitude !=0.0 && longitude!=0.0){
                latitude= lastKnownLocation.getLatitude();
                longitude= lastKnownLocation.getLongitude();
            }
        }

        latLng = new LatLng(latitude, longitude);
        marker= mMap.addMarker(new MarkerOptions().position(latLng).title("My position"));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 7.0f ) );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.getUiSettings().setCompassEnabled(true);

        circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.RED);
        circle = mMap.addCircle(circleOptions);
        circle.setVisible(true);

        currentZoomLevel = getZoomLevel(circle);
        animateZomm = currentZoomLevel ;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, animateZomm));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), 2000, null);

        puttingMarkers(getIntent());

        googleMap.setOnMarkerClickListener(this);

    }

    // chitaem JSON file s informatsiei iz papke assets
    public String loadJSONFromAsset() {
        Log.d("TESTING","loadJSONFromAsset");
        String json = null;
        try {
            InputStream is = this.getAssets().open("json.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.d("TESTING","IOException= "+ex);
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    // chitaem informatsiyu iz JSON filea
    public void parsingJSON(){
        try {
            JSONArray jsonArray= new JSONArray(loadJSONFromAsset());
            //JSONObject obj = new JSONObject(loadJSONFromAsset());

            for(int i=0; i<jsonArray.length();i++){
                JSONObject c = jsonArray.getJSONObject(i);
                //String CommonName = new String(c.getString("CommonName").getBytes("ISO-8859-1"), "UTF-8");
                String CommonName = c.getString("CommonName");
                String Category = c.getString("Category");
                String Location = c.getString("Location");
                String WebSite = c.getString("WebSite");
                //String Category = new String(c.getString("Category").getBytes("ISO-8859-1"), "UTF-8");
                Category= parseString(Category);

                JSONObject obj  = c.getJSONObject("geoData");
                String coordinates = obj.getString("coordinates");
                coordinates= coordinates.substring(1);
                coordinates= coordinates.substring(0, coordinates.length()-1);
                Log.d("TESTING","Location= "+Location);
                Log.d("TESTING","WebSite= "+WebSite);
                db = myDBHelper.getWritableDatabase();
                latitude_for_insert= coordinates.substring(coordinates.indexOf(","));
                longitude_for_insert= coordinates.substring(0,coordinates.indexOf(","));
                coordinates=latitude_for_insert+","+longitude_for_insert;
                coordinates= coordinates.substring(1);
                myDBHelper.insert(CommonName,Category,coordinates,Location,WebSite);
                //db.close();
//                    if(!category_list.contains(Category)){
//                        category_list.add(Category);
//                    }
//                    Log.d("TESTING","category_list.size()= "+category_list.size());

            }
        } catch (JSONException e) {
            Log.d("TESTING","JSONException= "+e);
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          if (!isStartApp){
              isStartApp = true;
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.INTERNET}
                          ,10);
              }
              return;
          }

        }
        else{

            // veshaem slushatel na obnovleniya koordinat

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //noinspection MissingPermission
            locationManager.requestLocationUpdates("gps", 5000, 1, listener);
            //noinspection MissingPermission
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, listener);
        }

    }

    // otmenyaem slushatel obnovlenei koordinat
    @Override
    protected void onPause() {
        if(locationManager!=null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
        super.onPause();
    }

    // chitaem iz JSON i menyaem vid teksta soderjashego dannye o koordinatax
    public LatLng parseLatLng(String coordinates){
        String[] separated = coordinates.split(",");
        String latitude= separated[0];
        String longitude= separated[1];
        LatLng latlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        Log.d("TESTING","latlng= "+latlng);
        return latlng;
    }

    // chitaem iz JSON i menyaem vid teksta soderjashego dannye o nazvanii
    public String parseString (String CommonName){
        String newCommonName= CommonName.replace("\"", "");
        newCommonName= newCommonName.replace("]","");
        newCommonName= newCommonName.replace("[","");
        return newCommonName;
    }

    // stavlyaem markery kategroii kotoryx byli vybrany
    public void puttingMarkers(Intent intent){
        Log.d("TESTING","puttingMarkers");
        db = myDBHelper.getWritableDatabase();



        if(intent!=null) {
            if (intent.hasExtra("radius") && intent.hasExtra("latLng")) {
                if(intent.hasExtra("checked_list")){
                    checked_list = (ArrayList<String>) intent.getSerializableExtra("checked_list");
                }

                radius = intent.getExtras().getInt("radius");
                latLng = intent.getParcelableExtra("latLng");
                marker.setPosition(latLng);
                circle.setCenter(latLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, animateZomm));
                circle.setRadius(radius);

                if(checked_list.size()>0){
                    cursor= myDBHelper.getBy_selected_categories(checked_list);
                    Log.d("TESTING","checked_list.size()>0");
                    if(cursor.getCount()>0){
                        Log.d("TESTING","cursor.getCount()>0");
                        while (cursor.moveToNext()){
                            if(radius>= SphericalUtil.computeDistanceBetween(latLng, parseLatLng(cursor.getString(3)))){
                                final Marker marker_place= mMap.addMarker(new MarkerOptions()
                                        .position(parseLatLng(cursor.getString(3)))
                                        .title(cursor.getString(1)));
                                marker_place.setDraggable(true);
                                Log.d("TESTING","adding");
                                marker_list.add(marker_place);
                            }
                        }
                        mMap.setOnMarkerClickListener(onMarkerClickListener);
                        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                            @Override
                            public void onMarkerDragStart(Marker marker) {
                                Log.d("TESTING","onMarkerDragStart");
//                                ResulteGoogleDialog resulteGoogleDialog = new ResulteGoogleDialog();
//                                resulteGoogleDialog.setParametr(distance, time, new ResulteGoogleDialog.DialogClike() {
//                                    @Override
//                                    public void dialogClikeStart() {
//                                        //CustomPreferences preferences = new CustomPreferences(MapsActivity.this,BarDateAdapter.preferencesCount);
//                                        preferences.putInt(BarDateAdapter.ALL_START,preferences.getInt(BarDateAdapter.ALL_START, 0) + 1);
//                                        //clikeStart.clikeStart(latLng);
//                                    }
//                                });
                            }

                            @Override
                            public void onMarkerDragEnd(Marker marker) {

                            }

                            @Override
                            public void onMarkerDrag(Marker marker) {
                                Log.d("TESTING","onMarkerDrag");
                            }
                        });

                        Log.d("TESTING","marker_list.size()= "+marker_list.size());
                    }
                }

            }
        }

        db.close();
    }

    GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clear, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        mMap.clear();
        latLng = new LatLng(latitude, longitude);
        marker= mMap.addMarker(new MarkerOptions().position(latLng).title("My position"));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 7.0f ) );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        tv_distance_address.setText("");
        tv_time_email.setText("");

        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
//        if (isOnline()){
            switch (item.getItemId()) {

//                case R.id.home: {
//                    Intent intent= new Intent(this,)
//                }
//                break;

//                case R.id.distance: {
//
//                }
//                break;

//                case R.id.add_point: {
//
//                }
//                break;

                case R.id.statictica: {
                    Intent intent= new Intent(this,StatisticsActivity.class);
                    startActivity(intent);
                }
                break;

//                case R.id.help: {
//
//                }
//                break;

                case R.id.nav_settings: {
                    if (category_list.size() == 0) {
                        db = myDBHelper.getWritableDatabase();
                        Cursor cursor_for_intent = myDBHelper.getAll();
                        if (cursor_for_intent.getCount() > 0) {
                            while (cursor_for_intent.moveToNext()) {
                                String Category = cursor_for_intent.getString(2);
                                if (!category_list.contains(Category)) {
                                    category_list.add(Category);
                                }
                            }
                        }
                        db.close();
                    }
                    Intent intent = new Intent(this, FilterActivity.class);
                    intent.putExtra("category_list", category_list);
                    intent.putExtra("latLng", latLng);
                    startActivityForResult(intent, 191);

                }
                break;
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
//        }
//
//        return false;

    }

    // karektiruem zoom ( masshtab )
    public float getZoomLevel(Circle circle) {
        float zoomLevel=0;
        if (circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel =(int) (16 - Math.log(scale) / Math.log(2));
        }
        Log.d("ZOOM_LEVEL","zoomLevel= "+zoomLevel);
        return zoomLevel;
    }

    // sobstvenno i sam slushatel obnovlenii
    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude= location.getLatitude();
            longitude= location.getLongitude();
            latLng = new LatLng(latitude, longitude);
            Log.d("TEST","marker= "+marker);
            Log.d("TEST","latitude= "+latitude);
            Log.d("TEST","longitude= "+longitude);
            if(marker!=null){
                marker.setPosition(latLng);
            }
            if(circle!=null){
                circle.setCenter(latLng);
            }
            if(mMap!=null){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, animateZomm));
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }
    };

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        else
        {
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle(getResources().getString(R.string.app_name))
                    .setMessage("Проверьте подключение к интернету и повторите попытку.")
                    .setPositiveButton("OK", null).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK){
            if (requestCode == 178){

            }else
                if (requestCode == 191){
                    puttingMarkers(data);
                }
        }

    }

    public void openDialog() {
        final Dialog dialog = new Dialog(this);
        getLayoutInflater().inflate(R.layout.dialog, null);
        dialog.setContentView(R.layout.dialog);

        Button btn_ok=(Button) dialog.findViewById(R.id.dialog_ok);
        Button btn_cancel=(Button) dialog.findViewById(R.id.dialog_cancel);
        TextView tv_info=(TextView) dialog.findViewById(R.id.dialog_info);
        dialog.setTitle("Информация");
        tv_distance_address.setText(df.format(distance)+" м");
        tv_time_email.setText(df.format(time)+" ч. (пешком)");
        tv_info.setText("Дистанция "+df.format(distance)+" м"+"\n"
        +"Продолжительность "+df.format(time)+" ч. (пешком)"+"\n"
        +"Адрес "+address+"\n"
        +"E-mail "+email+"\n");

        dialog.show();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = myDBHelper.getWritableDatabase();
                myDBHelper.update(id_target,count_current+1);
                db.close();

//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                        Uri.parse("http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+Double.parseDouble(latitude_target)+","+Double.parseDouble(longitude_target)));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER );
//                intent.setClassName("com.Lokos.moscowinstitutionssearch", "com.Lokos.moscowinstitutionssearch.MapsActivity");
//                startActivity(intent);

                LatLng origin= new LatLng(latitude, longitude);
                LatLng dest= new LatLng(Double.parseDouble(latitude_target), Double.parseDouble(longitude_target));

                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(origin, dest);

                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);

                dialog.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            Log.d("TESTING","result= "+result);
            Log.d("TESTING","result.size()= "+result.size());
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

}
