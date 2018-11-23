package com.maps.subwaytransit.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.adapter.SearchResultsAdapter;
import com.maps.subwaytransit.ads.InterstitialAdSingleton;
import com.maps.subwaytransit.application.MainApplication;
import com.maps.subwaytransit.model.SearchResults;
import com.maps.subwaytransit.preference.SharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetUserPreferenceLocationActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private Toolbar mToolbar;

    private ImageButton locationSaveButton, myLocationButton;

    private GoogleMap mGoogleMap;

    private SharedPreference mSharedPreference;

    private EditText placeSearch;

    private CountDownTimer countDownTimer;

    private ArrayList<SearchResults> resultsArrayList = new ArrayList<>();

    private List<Address> addresses;

    private Geocoder geocoder;

    private SearchResultsFragment searchResultsFragment;

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private String title;
    private boolean isLaunched = true;
    private double latitude, longitude;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;

    private AdView adView;

    private final Handler adsHandler = new Handler();

    private int timerValue = 3000, networkRefreshTime = 10000;
    private static final String LOG_TAG = "Ads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_preference_location);

        title = getIntent().getStringExtra("title");

        mLocationRequest = new LocationRequest();

        mSharedPreference = new SharedPreference(this);

        geocoder = new Geocoder(this);

        initializeView();

        initializeAds();

        showInterstitialAd();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.set_user_preference_location_map);
        mapFragment.getMapAsync(this);

        locationSaveButton.setOnClickListener(this);
        myLocationButton.setOnClickListener(this);

        placeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeSearch.setText("");
            }
        });

        placeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placeSearch.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_pastel_tones));

        countDownTimer = new CountDownTimer(1500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                placeSearch.setText("");

                placeSearch.setHint("  Fetching place...");

                placeSearch.setCursorVisible(false);
            }

            @Override
            public void onFinish() {
                try {
                    LatLng center = mGoogleMap.getCameraPosition().target;
                    addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1);

                    for (int i = 0; i < addresses.size(); i++) {
                        StringBuilder address = new StringBuilder();

                        if (addresses.get(i).getThoroughfare() != null) {
                            address.append(" ").append(addresses.get(i).getThoroughfare());
                        } else {
                            address.append(addresses.get(i).getFeatureName());
                        }

                        if (addresses.get(i).getLocality() != null) {
                            address.append(", ").append(addresses.get(i).getLocality());
                        }

                        placeSearch.setText(address);

                        placeSearch.setHint("  Search Place");

                        placeSearch.setCursorVisible(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SetUserPreferenceLocationActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        };

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                countDownTimer.cancel();

                countDownTimer.start();
            }
        });

        mFusedLocationProviderClient = MainApplication.getFusedLocationProviderClient();

        if (!mSharedPreference.getUserMockLocationMode()) {
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            createLocationCallback();

            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } else {
            mFusedLocationProviderClient.setMockMode(true);

            LatLng mockLatLng = mSharedPreference.getUserMockLocation();

            Location location = new Location("Mock");
            location.setLatitude(mockLatLng.latitude);
            location.setLongitude(mockLatLng.longitude);
            location.setTime(System.currentTimeMillis());
            location.setAccuracy(Criteria.ACCURACY_FINE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }

            mFusedLocationProviderClient.setMockLocation(location);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            createLocationCallback();

            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        }

        placeSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    placeSearch.setCursorVisible(false);

                    searchPlace(v.getText().toString());
                }
                return false;
            }
        });

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (searchResultsFragment != null) {
                    if (searchResultsFragment.isVisible()) {
                        searchResultsFragment.dismiss();
                    }
                }
            }
        });
    }

    private void initializeView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

        locationSaveButton = (ImageButton) findViewById(R.id.location_save_button);
        myLocationButton = (ImageButton) findViewById(R.id.my_location_button);

        placeSearch = (EditText) findViewById(R.id.place_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Drawable searchIcon = getResources().getDrawable(R.drawable.search_icon, null);
            placeSearch.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_save_button: {
                if (title.equals("Set Home")) {
                    mSharedPreference.setUserHomeLocation(mGoogleMap.getCameraPosition().target.latitude, mGoogleMap.getCameraPosition().target.longitude);

                    mSharedPreference.setUserHomeLocationName(placeSearch.getText().toString());

                    Toast.makeText(SetUserPreferenceLocationActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                    finish();
                } else if (title.equals("Set Work")) {
                    mSharedPreference.setUserWorkLocation(mGoogleMap.getCameraPosition().target.latitude, mGoogleMap.getCameraPosition().target.longitude);

                    mSharedPreference.setUserWorkLocationName(placeSearch.getText().toString());

                    Toast.makeText(SetUserPreferenceLocationActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
            break;
            case R.id.my_location_button: {
                LatLng latLng = new LatLng(latitude, longitude);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mGoogleMap.getCameraPosition().zoom);

                mGoogleMap.animateCamera(cameraUpdate);
            }
            break;
        }
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                try {
                    Location location = locationResult.getLastLocation();

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    if (isLaunched) {
                        isLaunched = false;

                        LatLng latLng = new LatLng(latitude, longitude);

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);

                        mGoogleMap.animateCamera(cameraUpdate);
                    }
                } catch (NullPointerException ex) {
                    Toast.makeText(SetUserPreferenceLocationActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }

        return false;
    }

    private void searchPlace(String placeName) {
        placeName = placeName.replaceAll("\\s", "%20");

        String searchUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + placeName + "&location=40.67,-73.94&radius=10000&key=" + getString(R.string.places_api_key);

        JsonObjectRequest placeSearchRequest = new JsonObjectRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("SearchResult", response.toString());

                if (response.has("status")) {
                    try {
                        String status = response.getString("status");

                        if (status.equalsIgnoreCase("OK")) {
                            JSONArray resultArray = response.getJSONArray("results");

                            resultsArrayList.clear();

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject resultData = resultArray.getJSONObject(i);

                                String address = resultData.getString("formatted_address");

                                JSONObject geometryData = resultData.getJSONObject("geometry");

                                String placeName = resultData.getString("name");

                                JSONObject locationData = geometryData.getJSONObject("location");

                                String lat = locationData.getString("lat");
                                String lng = locationData.getString("lng");

                                SearchResults searchResults = new SearchResults();
                                searchResults.setTitle(placeName);
                                searchResults.setLat(lat);
                                searchResults.setLng(lng);
                                searchResults.setAddress(address);

                                resultsArrayList.add(searchResults);
                            }

                            if (resultsArrayList.size() == 1) {
                                JSONObject resultData = resultArray.getJSONObject(0);

                                JSONObject geometryData = resultData.getJSONObject("geometry");

                                String placeName = resultData.getString("name");

                                JSONObject locationData = geometryData.getJSONObject("location");

                                String lat = locationData.getString("lat");
                                String lng = locationData.getString("lng");

                                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                                mGoogleMap.animateCamera(cameraUpdate);
                            } else {
                                searchResultsFragment = new SearchResultsFragment(SetUserPreferenceLocationActivity.this, mGoogleMap, resultsArrayList);
                                searchResultsFragment.show(getSupportFragmentManager(), "search_result_fragment");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(placeSearchRequest);
    }

    @SuppressLint("ValidFragment")
    public static class SearchResultsFragment extends DialogFragment {
        private Context context;

        private GoogleMap googleMap;

        private ArrayList<SearchResults> resultsArrayList = new ArrayList<>();

        private ListView searchResultsView;

        private SearchResultsAdapter adapter;

        public SearchResultsFragment(Context context, GoogleMap googleMap, ArrayList<SearchResults> resultsArrayList) {
            this.context = context;
            this.googleMap = googleMap;
            this.resultsArrayList = resultsArrayList;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            adapter = new SearchResultsAdapter(context, resultsArrayList);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);

            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            return dialog;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_dialog_fragment_search_results, container);

            initializeView(view);

            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            searchResultsView.setAdapter(adapter);

            searchResultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LatLng latLng = new LatLng(Double.parseDouble(resultsArrayList.get(position).getLat()), Double.parseDouble(resultsArrayList.get(position).getLng()));

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                    googleMap.animateCamera(cameraUpdate);
                }
            });
        }

        private void initializeView(View view) {
            searchResultsView = (ListView) view.findViewById(R.id.line_search_results_view);
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

        locationSaveButton.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(placeSearch.getWindowToken(), 0);

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