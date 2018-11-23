package com.maps.subwaytransit.ads;

import android.content.Context;

import com.maps.subwaytransit.interfaces.InterstitialAdListener;

public class InterstitialAdSingleton implements InterstitialAdListener {
    private static InterstitialAdSingleton mInstance = null;

    private GoogleInterstitialAds mGoogleAds;

    private Context mContext;

    private InterstitialAdListener mInterstitialListener;

    public static InterstitialAdSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new InterstitialAdSingleton(context);
        }

        return mInstance;
    }

    private InterstitialAdSingleton(Context context) {
        mContext = context.getApplicationContext();

        mGoogleAds = new GoogleInterstitialAds(mContext);
        mGoogleAds.setInterstitialAdListener(this);
    }

    public void firstInterstitialLoad() {
        mGoogleAds.callInterstitialAds(false);
    }

    public void setInterstitialCloseListener(InterstitialAdListener mInterstitialListener) {
        this.mInterstitialListener = mInterstitialListener;
    }

    public void showInterstitial() {
        mGoogleAds.showInterstitialAds();
    }

    public boolean isInterstitialAdLoaded() {
        return mGoogleAds.isInterstitialAdLoaded();
    }

    public void cancelInterstitialAd() {
        mGoogleAds.cancelInterstitialAds();
    }

    @Override
    public void adClosed() {
        if (mInterstitialListener != null)
            mInterstitialListener.adClosed();
    }
}
