package com.Lokos.moscowinstitutionssearch;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Created by User on 6/3/2017.
 */

public class MyApplication extends Application {

    boolean isAppCreate = false;
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("aaaaaaaaaa", "OnStarttttttttttttttt");
        isAppCreate = true;
    }


    public boolean isAppCreate() {
        return isAppCreate;
    }

    public void setAppCreate(boolean appCreate) {
        isAppCreate = appCreate;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return super.getApplicationInfo();
    }
}
