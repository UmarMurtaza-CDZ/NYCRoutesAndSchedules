package com.maps.subwaytransit.activity;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.adapter.LineRouteStationsAdapter;
import com.maps.subwaytransit.adapter.StationsInfoWindowAdapter;
import com.maps.subwaytransit.analytics.Analytics;
import com.maps.subwaytransit.database.GTFSFeedDatabase;
import com.maps.subwaytransit.model.LineRouteModel;
import com.maps.subwaytransit.view.BottomSheetListView;

import java.util.ArrayList;
import java.util.List;

public class DrawLineRouteActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener {
    private Toolbar mToolbar;

    int index;
    private String lineName, color, desiredStation;

    private ImageButton sheetDragArrow;

    private GoogleMap mMap;

    private Marker mMarker;

    private BottomSheetBehavior routeStationsSheet;

    private BottomSheetListView lineRouteStationsView;

    private LineRouteStationsAdapter adapter;

    private ArrayList<LineRouteModel> lineRouteArrayList = new ArrayList<>();
    private List<LatLng> routeStopsList = new ArrayList<>();
    private ArrayList<String> shapeId = new ArrayList<>();
    private ArrayList<String> lineRouteStationsList = new ArrayList<>();
    private ArrayList<Marker> stationMarkers = new ArrayList<>();

    private GTFSFeedDatabase database;

    private Analytics mAnalytics;

    private AdView adView;

    private final Handler adsHandler = new Handler();

    private int timerValue = 3000, networkRefreshTime = 10000;
    private static final String LOG_TAG = "Ads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_line_route);

        desiredStation = getIntent().getStringExtra("station_name");
        lineName = getIntent().getStringExtra("line");
        color = getIntent().getStringExtra("color");

        database = new GTFSFeedDatabase(this);

        mAnalytics = new Analytics(this);

        initializeView();

        initializeAds();

        if (lineName.equalsIgnoreCase("SIR")) {
            lineRouteArrayList = database.getLineRoute("SI");
        } else {
            lineRouteArrayList = database.getLineRoute(lineName);
        }

        for (int i = 0; i < lineRouteArrayList.size(); i++) {
            if (!shapeId.contains(lineRouteArrayList.get(i).getShapeId())) {
                shapeId.add(lineRouteArrayList.get(i).getShapeId());
            }

            lineRouteStationsList.add(lineRouteArrayList.get(i).getStopName());
        }

        adapter = new LineRouteStationsAdapter(this, lineRouteStationsList, desiredStation);

        lineRouteStationsView.setAdapter(adapter);

        lineRouteStationsView.setOnItemClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.route_line_map);
        mapFragment.getMapAsync(this);

        if (desiredStation != null) {
            routeStationsSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

            for (int i = 0; i < lineRouteArrayList.size(); i++) {
                if (lineRouteArrayList.get(i).getStopName().equals(desiredStation)) {
                    index = i;
                }
            }

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    try {
                        lineRouteStationsView.smoothScrollToPosition(index);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
        }

        routeStationsSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

            }

            @Override
            public void onSlide(@NonNull View view, float slideOffset) {
                animateBottomSheetArrows(slideOffset);
            }
        });

        sheetDragArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (routeStationsSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    routeStationsSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else if (routeStationsSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    routeStationsSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        mAnalytics.sendScreenAnalytics(this, "Subway Line Draw Screen");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_pastel_tones));

        mMap.setInfoWindowAdapter(new StationsInfoWindowAdapter(this));


        for (int i = 0; i < shapeId.size(); i++) {
            routeStopsList.clear();

            for (int j = 0; j < lineRouteArrayList.size(); j++) {
                if (lineRouteArrayList.get(j).getShapeId().equalsIgnoreCase(shapeId.get(i))) {
                    routeStopsList.add(lineRouteArrayList.get(j).getStopLatLng());
                }
            }

            mMap.addPolyline(new PolylineOptions()
                    .addAll(routeStopsList)
                    .width(15)
                    .color(Color.parseColor("#" + color))
                    .zIndex(10));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                builder.include(new LatLng(routeStopsList.get(0).latitude, routeStopsList.get(0).longitude));
                builder.include(new LatLng(routeStopsList.get(routeStopsList.size() - 1).latitude, routeStopsList.get(routeStopsList.size() - 1).longitude));

                for (int i = 0; i < lineRouteArrayList.size(); i++) {
                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(lineRouteArrayList.get(i).getStopLatLng())
                            .title(lineRouteArrayList.get(i).getStopName())
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromAsset("ic_stops.png")));

                    if (mMarker.getTitle().equals(lineRouteStationsList.get(index))) {
                        if (!mMarker.isInfoWindowShown()) {
                            mMarker.showInfoWindow();
                        }
                    }

                    stationMarkers.add(mMarker);
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), (int) getResources().getDimension(R.dimen._100sdp)));
            }
        }, 1000);
    }

    private void initializeView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(lineName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#" + color));
        }

        mToolbar.setBackgroundColor(Color.parseColor("#" + color));

        sheetDragArrow = (ImageButton) findViewById(R.id.sheet_drag_arrow);

        routeStationsSheet = BottomSheetBehavior.from(findViewById(R.id.route_stations_sheet));

        RelativeLayout sheetBackground = (RelativeLayout) findViewById(R.id.route_stations_sheet);
        sheetBackground.setBackgroundColor(Color.parseColor("#" + color));

        lineRouteStationsView = (BottomSheetListView) findViewById(R.id.line_route_stations_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAnalytics.sendEventAnalytics("Subway Station", "Tapped");

        if (routeStationsSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            routeStationsSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        stationMarkers.get(position).showInfoWindow();

        LatLng latLng = new LatLng(stationMarkers.get(position).getPosition().latitude, stationMarkers.get(position).getPosition().longitude);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    private void animateBottomSheetArrows(float slideOffset) {
        sheetDragArrow.setRotation(slideOffset * 180);
    }

    @Override
    public void onBackPressed() {
        if (routeStationsSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            routeStationsSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
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
