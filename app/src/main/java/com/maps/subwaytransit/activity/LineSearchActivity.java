package com.maps.subwaytransit.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.adapter.LineSearchResultsAdapter;
import com.maps.subwaytransit.analytics.Analytics;
import com.maps.subwaytransit.database.GTFSFeedDatabase;
import com.maps.subwaytransit.model.RouteModel;

import java.util.ArrayList;

public class LineSearchActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private EditText lineSearch;

    private ListView lineSearchResultsView;

    private LinearLayout noResultsLayout;

    private LineSearchResultsAdapter adapter;

    private ArrayList<RouteModel> routeModelArrayList = new ArrayList<>();

    private GTFSFeedDatabase database;

    private Analytics mAnalytics;

    private AdView adView;

    private final Handler adsHandler = new Handler();

    private int timerValue = 3000, networkRefreshTime = 10000;
    private static final String LOG_TAG = "Ads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_search);

        database = new GTFSFeedDatabase(this);

        mAnalytics = new Analytics(this);

        initializeView();

        initializeAds();

        routeModelArrayList = database.getAllLines();

        adapter = new LineSearchResultsAdapter(LineSearchActivity.this, routeModelArrayList);

        lineSearchResultsView.setAdapter(adapter);

        lineSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!lineSearch.getText().toString().isEmpty()) {
                    routeModelArrayList = database.getLineName(s.toString());

                    if (routeModelArrayList.size() > 0) {

                        adapter = new LineSearchResultsAdapter(LineSearchActivity.this, routeModelArrayList);

                        lineSearchResultsView.setAdapter(adapter);

                        noResultsLayout.setVisibility(View.GONE);
                    } else {
                        lineSearchResultsView.setAdapter(null);

                        noResultsLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    routeModelArrayList = database.getAllLines();

                    adapter = new LineSearchResultsAdapter(LineSearchActivity.this, routeModelArrayList);

                    lineSearchResultsView.setAdapter(adapter);

                    noResultsLayout.setVisibility(View.GONE);
                }
            }
        });

        lineSearchResultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAnalytics.sendEventAnalytics("Subway Line", "Tapped");
                Intent drawLineRouteIntent = new Intent(LineSearchActivity.this, DrawLineRouteActivity.class);
                drawLineRouteIntent.putExtra("line", routeModelArrayList.get(position).getRouteId());
                drawLineRouteIntent.putExtra("color", routeModelArrayList.get(position).getRouteColor());
                startActivity(drawLineRouteIntent);
            }
        });

        mAnalytics.sendScreenAnalytics(this, "Subway Line Search Screen");
    }

    private void initializeView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.find_a_line);

        lineSearch = (EditText) findViewById(R.id.line_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Drawable searchIcon = getResources().getDrawable(R.drawable.search_icon, null);
            lineSearch.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
        }

        lineSearch.setClipToOutline(true);

        lineSearchResultsView = (ListView) findViewById(R.id.line_search_results_view);

        noResultsLayout = (LinearLayout) findViewById(R.id.no_results_layout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }

        return false;
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
}
