package com.maps.subwaytransit.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.ads.InterstitialAdSingleton;
import com.maps.subwaytransit.analytics.Analytics;
import com.maps.subwaytransit.fragment.HomeFragment;
import com.maps.subwaytransit.preference.SharedPreference;

public class MainActivity extends AppCompatActivity {
    public DrawerLayout mDrawerLayout;

    public NavigationView mNavigationView;

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private SharedPreference mSharedPreference;

    private Analytics mAnalytics;

    private AdView adView;

    private final Handler adsHandler = new Handler();

    private int timerValue = 3000, networkRefreshTime = 10000;
    private static final String LOG_TAG = "Ads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreference = new SharedPreference(this);

        mAnalytics = new Analytics(this);

        initializeView();

        initializeAds();

        showInterstitialAd();

        setDrawerContent(mNavigationView);

        mActionBarDrawerToggle = setupDrawerToggle();
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new HomeFragment()).commit();
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }

        if (mSharedPreference.isFirstRun()) {
            mSharedPreference.setFirstRun(false);

            AlertDialog infoAlert = new AlertDialog.Builder(this)
                    .setMessage("Currently this app only supports the subway system of the New York city, you can find subway station markers on the map and tap on them to retrieve the further info.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void initializeView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
    }

    private void setDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }

    private void selectDrawerItem(MenuItem item) {
        if (item.isChecked()) {
            mDrawerLayout.closeDrawers();
        } else {
            switch (item.getItemId()) {
                case R.id.home: {
                    mAnalytics.sendEventAnalytics("Drawer Home Button", "Tapped");
                    if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new HomeFragment())
                                .commit();
                    } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                        getSupportFragmentManager().popBackStack();
                    }
                    break;
                }
                case R.id.privacy_policy: {
                    mAnalytics.sendEventAnalytics("Drawer Privacy Policy Button", "Tapped");
                    String privacyPolicyLink = "https://nazmainapps.blogspot.com/2017/12/welcome-note.html";
                    Intent privacyPolicyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyLink));
                    startActivity(privacyPolicyIntent);
                }
                break;
                case R.id.share: {
                    mAnalytics.sendEventAnalytics("Drawer Share Button", "Tapped");
                    String message = "You need NYC Subway - Routes & Schedule App! Download it for free:\n" + "https://play.google.com/store/apps/details?id=" + getPackageName();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                }
                break;
                case R.id.rate_us: {
                    mAnalytics.sendEventAnalytics("Drawer Rate Us Button", "Tapped");
                    Intent rateUsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                    startActivity(rateUsIntent);
                }
                break;
            }

            mDrawerLayout.closeDrawers();
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, null, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (fragment instanceof HomeFragment) {
            if (((HomeFragment) fragment).appMenuSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                ((HomeFragment) fragment).appMenuSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                if (!mSharedPreference.getUserFeedback()) {
                    feedbackDialog();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    private void feedbackDialog() {
        AlertDialog.Builder feedbackDialog = new AlertDialog.Builder(this);

        feedbackDialog.setTitle(getString(R.string.feedback));
        feedbackDialog.setMessage(getString(R.string.feedback_text));

        feedbackDialog.setPositiveButton(getString(R.string.give_feedback), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uriUrl = Uri.parse("https://market.android.com/details?id=" + getPackageName());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                mSharedPreference.setUserFeedback(true);
            }
        });

        feedbackDialog.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                } else {
                    finish();
                }
            }
        });

        feedbackDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    MainActivity.this.finish();
                }
                return true;
            }
        });

        AlertDialog feedbackPopUp = feedbackDialog.create();

        feedbackPopUp.show();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable());
    }

    @Override
    protected void onResume() {
        super.onResume();

        startAdsCall();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopAdsCall();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        destroyAds();
    }

    private Runnable sendUpdatesAdsToUI = new Runnable() {
        public void run() {
            Log.v(LOG_TAG, "Recall");
            updateUIAds();
        }
    };

    private void initializeAds() {

        adView = (AdView) findViewById(R.id.adView);
        adView.setVisibility(View.GONE);

        if (isNetworkConnected()) {
            this.adView.setVisibility(View.VISIBLE);
        } else {
            this.adView.setVisibility(View.GONE);
        }
        setAdsListener();
    }

    private final void updateUIAds() {
        if (isNetworkConnected()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            timerValue = networkRefreshTime;
            adsHandler.removeCallbacks(sendUpdatesAdsToUI);
            adsHandler.postDelayed(sendUpdatesAdsToUI, timerValue);
        }
    }

    public void startAdsCall() {
        Log.i(LOG_TAG, "Starts");
        if (isNetworkConnected()) {
            this.adView.setVisibility(View.VISIBLE);
        } else {
            this.adView.setVisibility(View.GONE);
        }

        adView.resume();
        adsHandler.removeCallbacks(sendUpdatesAdsToUI);
        adsHandler.postDelayed(sendUpdatesAdsToUI, 0);
    }

    public void stopAdsCall() {
        Log.e(LOG_TAG, "Ends");
        adsHandler.removeCallbacks(sendUpdatesAdsToUI);
        adView.pause();
    }

    public void destroyAds() {
        Log.e(LOG_TAG, "Destroy");
        adView.destroy();
        adView = null;
    }

    private void setAdsListener() {
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(LOG_TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int error) {
                String message = "onAdFailedToLoad: " + getErrorReason(error);
                Log.d(LOG_TAG, message);
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(LOG_TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                Log.d(LOG_TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                Log.d(LOG_TAG, "onAdLoaded");
                adView.setVisibility(View.VISIBLE);

            }
        });
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

    private void showInterstitialAd() {
        InterstitialAdSingleton mInterstitialAdSingleton = InterstitialAdSingleton.getInstance(this);

        mInterstitialAdSingleton.showInterstitial();
    }
}
