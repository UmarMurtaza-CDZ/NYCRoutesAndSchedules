package com.maps.subwaytransit.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.maps.subwaytransit.ads.InterstitialAdSingleton;

public class MainApplication extends MultiDexApplication {
    private static FusedLocationProviderClient mFusedLocationProviderClient;

    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        InterstitialAdSingleton mInterstitialAdSingleton = InterstitialAdSingleton.getInstance(this);
        mInterstitialAdSingleton.firstInterstitialLoad();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    public static synchronized FusedLocationProviderClient getFusedLocationProviderClient() {
        if (mFusedLocationProviderClient == null) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }

        return mFusedLocationProviderClient;
    }
}