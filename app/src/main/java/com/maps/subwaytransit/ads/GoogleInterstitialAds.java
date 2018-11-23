package com.maps.subwaytransit.ads;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.interfaces.InterstitialAdListener;

public class GoogleInterstitialAds {
    private Context context;

    private static final String LOG_TAG = "Ads-Interstitial";
    private boolean isShowInterstitial = false;
    private boolean isInterstitialLoaded = false;

    private InterstitialAd mInterstitialAd;

    private InterstitialAdListener listener = null;

    public GoogleInterstitialAds(Context context) {
        this.context = context;
        isInterstitialLoaded = false;
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        this.listener = listener;
    }

    public void callInterstitialAds(boolean showAd) {
        final boolean checkShowAd = showAd;

        final String adId = context.getString(R.string.interstitial_id);

        isInterstitialLoaded = false;
        isShowInterstitial = true;

        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(adId);

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        mInterstitialAd.loadAd(adRequestBuilder.build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.v("interstitial Ad", "Loaded");

                isInterstitialLoaded = true;

                if (checkShowAd && isShowInterstitial)
                    showInterstitialAds();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                isInterstitialLoaded = false;

                callInterstitialAds(checkShowAd);

                String message = "onAdFailedToLoad: " + getErrorReason(errorCode);

                Log.d(LOG_TAG, message);
            }

            @Override
            public void onAdClosed() {
                Log.d("interstitial Ad", "Closed");

                isInterstitialLoaded = false;

                if (listener != null)
                    listener.adClosed();
            }
        });
    }

    public void showInterstitialAds() {
        isShowInterstitial = true;

        isInterstitialLoaded = false;

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            callInterstitialAds(false);
        } else {
            Log.e("interstitial Ad", "No Ad");
            if (listener != null)
                listener.adClosed();
        }
    }

    public void cancelInterstitialAds() {
        isShowInterstitial = false;
    }

    public boolean isInterstitialAdLoaded() {

        return isInterstitialLoaded;
    }

    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }

        return errorReason;
    }
}

