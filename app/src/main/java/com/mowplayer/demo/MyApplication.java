package com.mowplayer.demo;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class MyApplication extends MultiDexApplication {

    private static MyApplication mInstance;

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {


        MultiDex.install(this);
        super.onCreate();
        try {
//            Fabric fabric = new Fabric.Builder(this)
//                    .kits(new Crashlytics())
//                    .debuggable(true)
//                    .build();
//            Fabric.with(this, new Crashlytics());
            mInstance = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }
}