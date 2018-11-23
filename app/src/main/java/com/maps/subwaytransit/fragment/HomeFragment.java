package com.maps.subwaytransit.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.activity.DrawLineRouteActivity;
import com.maps.subwaytransit.activity.LineSearchActivity;
import com.maps.subwaytransit.activity.MainActivity;
import com.maps.subwaytransit.activity.SetUserPreferenceLocationActivity;
import com.maps.subwaytransit.activity.UserMockLocationSetActivity;
import com.maps.subwaytransit.adapter.InboundSubwayAdapter;
import com.maps.subwaytransit.adapter.OutboundSubwayAdapter;
import com.maps.subwaytransit.adapter.SearchResultsAdapter;
import com.maps.subwaytransit.adapter.StationsInfoWindowAdapter;
import com.maps.subwaytransit.analytics.Analytics;
import com.maps.subwaytransit.application.MainApplication;
import com.maps.subwaytransit.database.GTFSFeedDatabase;
import com.maps.subwaytransit.model.SearchResults;
import com.maps.subwaytransit.model.StationScheduleModel;
import com.maps.subwaytransit.preference.SharedPreference;
import com.maps.subwaytransit.utils.SubwayIconRenderer;
import com.maps.subwaytransit.utils.SubwayStationsMarker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private MapView mapView;

    private ImageButton drawerIcon, myLocationButton, trafficInfoButton, findNearestSubway, setUserHomeButton, setUserWorkButton, userHomeDelete, userWorkDelete, sheetDragArrow;

    private GoogleMap googleMap;

    private Button timeToReachButton;

    private TextView locationText, userHomeLocation, userWorkLocation;

    private Geocoder geocoder;

    private CountDownTimer countDownTimer;

    private EditText placeSearch;

    public BottomSheetBehavior appMenuSheet;

    private RelativeLayout lineSearchLayout, outOfCityLayout, userHomeLayout, userWorkLayout, mockLocationIndicatorLayout;

    private ProgressBar locationLoadingBar;

    private SearchResultsFragment searchResultsFragment;

    private ClusterManager<SubwayStationsMarker> clusterManager;

    private ProgressBar loadingBar;

    private static Marker searchedMarker;
    private Marker homeMarker, workMarker;

    private Polyline routeLine;

    private List<LatLng> decodedRoutePolyLine = new ArrayList<LatLng>();
    private List<Address> addresses = new ArrayList<>();
    private List<Address> currentLocation = new ArrayList<>();

    private ArrayList<SearchResults> resultsArrayList = new ArrayList<>();
    private ArrayList<StationScheduleModel> outboundSubwaySchedule;
    private ArrayList<StationScheduleModel> inboundSubwaySchedule;

    private OutboundSubwayAdapter outboundSubwayAdapter;
    private InboundSubwayAdapter inboundSubwayAdapter;

    private ListView outboundSubwayList;
    private ListView inboundSubwayList;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;

    private GTFSFeedDatabase database;

    private SharedPreference mSharedPreference;

    private Analytics mAnalytics;

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private String stationName, reachTime;
    private double latitude, longitude;
    private boolean isLaunched = true;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        geocoder = new Geocoder(getContext());

        mLocationRequest = new LocationRequest();

        mSharedPreference = new SharedPreference(getContext());

        mAnalytics = new Analytics(getContext());

        database = new GTFSFeedDatabase(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_home, viewGroup, false);

        initializeView(view);

        mapView.onCreate(bundle);

        mapView.onResume();

        try {
            MapsInitializer.initialize(getContext());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }

        drawerIcon.setOnClickListener(this);
        myLocationButton.setOnClickListener(this);
        trafficInfoButton.setOnClickListener(this);
        findNearestSubway.setOnClickListener(this);
        setUserHomeButton.setOnClickListener(this);
        setUserWorkButton.setOnClickListener(this);
        userHomeDelete.setOnClickListener(this);
        userWorkDelete.setOnClickListener(this);
        lineSearchLayout.setOnClickListener(this);
        mockLocationIndicatorLayout.setOnClickListener(this);
        sheetDragArrow.setOnClickListener(this);

        mapView.getMapAsync(this);

        mAnalytics.sendScreenAnalytics(getActivity(), "Main Map Screen");

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.setOnMapLoadedCallback(this);

        googleMap.setInfoWindowAdapter(new StationsInfoWindowAdapter(getContext()));

        googleMap.setMyLocationEnabled(true);

        clusterManager = new ClusterManager<>(getContext(), googleMap);
        clusterManager.setRenderer(new SubwayIconRenderer(getContext(), googleMap, clusterManager));

        countDownTimer = new CountDownTimer(1500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                locationLoadingBar.setVisibility(View.VISIBLE);

                locationText.setText("");
            }

            @Override
            public void onFinish() {
                try {
                    locationLoadingBar.setVisibility(View.GONE);

                    LatLng center = googleMap.getCameraPosition().target;
                    addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1);

                    if (addresses.size() > 0) {
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

                            locationText.setText(address);
                        }
                    } else {
                        locationText.setText("Unnamed Location");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        };

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

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

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setTrafficEnabled(false);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.mapstyle_pastel_tones));

        drawStationsMarkers();

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                countDownTimer.cancel();

                countDownTimer.start();

                clusterManager.onCameraIdle();
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<SubwayStationsMarker>() {
            @Override
            public boolean onClusterItemClick(final SubwayStationsMarker marker) {
                getReachTime(marker.getPosition().latitude, marker.getPosition().longitude, marker.getTitle(), true);

                return false;
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mAnalytics.sendEventAnalytics("Subway Station Marker", "Tapped");

                clusterManager.onMarkerClick(marker);

                getReachTime(marker.getPosition().latitude, marker.getPosition().longitude, marker.getTitle(), false);

                return false;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (routeLine != null) {
                    routeLine.remove();
                }

                placeSearch.setCursorVisible(false);
            }
        });

        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (searchResultsFragment != null) {
                    if (searchResultsFragment.isVisible()) {
                        searchResultsFragment.dismiss();
                    }
                }
            }
        });

        appMenuSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.e("Bottom Sheet Behaviour", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        if (timeToReachButton.getVisibility() == View.VISIBLE) {
                            Animation popOut = AnimationUtils.loadAnimation(getContext(), R.anim.pop_out);

                            timeToReachButton.startAnimation(popOut);

                            popOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    timeToReachButton.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                        Log.e("Bottom Sheet Behaviour", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.e("Bottom Sheet Behaviour", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.e("Bottom Sheet Behaviour", "STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.e("Bottom Sheet Behaviour", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                animateBottomSheetArrows(slideOffset);
            }
        });

        placeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appMenuSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    appMenuSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        placeSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mAnalytics.sendEventAnalytics("Search Location EditText", "Location Searched");
                    placeSearch.setCursorVisible(false);

                    searchPlace(v.getText().toString());
                }
                return false;
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

    private void initializeView(View view) {
        drawerIcon = (ImageButton) view.findViewById(R.id.drawer_button);
        myLocationButton = (ImageButton) view.findViewById(R.id.my_location_button);
        trafficInfoButton = (ImageButton) view.findViewById(R.id.traffic_info_button);
        findNearestSubway = (ImageButton) view.findViewById(R.id.nearest_subway_button);
        setUserHomeButton = (ImageButton) view.findViewById(R.id.get_me_home_button);
        setUserWorkButton = (ImageButton) view.findViewById(R.id.get_me_to_work_button);
        userHomeDelete = (ImageButton) view.findViewById(R.id.user_home_delete);
        userWorkDelete = (ImageButton) view.findViewById(R.id.user_work_delete);
        sheetDragArrow = (ImageButton) view.findViewById(R.id.sheet_drag_arrow);

        locationText = (TextView) view.findViewById(R.id.location_text);
        userHomeLocation = (TextView) view.findViewById(R.id.home_coordinates);
        userWorkLocation = (TextView) view.findViewById(R.id.work_coordinates);

        locationText.setSelected(true);

        loadingBar = (ProgressBar) view.findViewById(R.id.loading_bar);

        locationLoadingBar = (ProgressBar) view.findViewById(R.id.location_loading_bar);

        mapView = (MapView) view.findViewById(R.id.map_view);

        timeToReachButton = (Button) view.findViewById(R.id.time_to_reach_button);

        appMenuSheet = BottomSheetBehavior.from(view.findViewById(R.id.app_menu_sheet));

        placeSearch = (EditText) view.findViewById(R.id.place_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Drawable searchIcon = getContext().getResources().getDrawable(R.drawable.search_icon, null);
            placeSearch.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
        }

        lineSearchLayout = (RelativeLayout) view.findViewById(R.id.line_search_layout);
        outOfCityLayout = (RelativeLayout) view.findViewById(R.id.out_of_city_layout);
        userHomeLayout = (RelativeLayout) view.findViewById(R.id.user_home_layout);
        userWorkLayout = (RelativeLayout) view.findViewById(R.id.user_work_layout);
        mockLocationIndicatorLayout = (RelativeLayout) view.findViewById(R.id.mock_location_indicator_layout);

        if (mSharedPreference.getUserMockLocationMode()) {
            mockLocationIndicatorLayout.setVisibility(View.VISIBLE);
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

                    try {
                        currentLocation = geocoder.getFromLocation(latitude, longitude, 1);

                        String city = currentLocation.get(0).getAdminArea();

                        Log.e("City", city);

                        if (city.equalsIgnoreCase("New York")) {
                            outOfCityLayout.setVisibility(View.GONE);
                        } else {
                            outOfCityLayout.setVisibility(View.VISIBLE);
                        }

                        if (isLaunched) {
                            isLaunched = false;

                            LatLng latLng = new LatLng(latitude, longitude);

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);

                            googleMap.animateCamera(cameraUpdate);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_button: {
                mAnalytics.sendEventAnalytics("Drawer Button", "Tapped");
                if (((MainActivity) getActivity()).mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    ((MainActivity) getActivity()).mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    ((MainActivity) getActivity()).mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
            break;
            case R.id.my_location_button: {
                mAnalytics.sendEventAnalytics("My Location Button", "Tapped");

                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                }

                LatLng latLng = new LatLng(latitude, longitude);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getCameraPosition().zoom);

                googleMap.animateCamera(cameraUpdate);

                if (routeLine != null) {
                    routeLine.remove();
                }

                if (searchedMarker != null) {
                    searchedMarker.remove();
                }

                placeSearch.setText("");
                placeSearch.setCursorVisible(false);

                if (timeToReachButton.getVisibility() == View.VISIBLE) {
                    Animation popOut = AnimationUtils.loadAnimation(getContext(), R.anim.pop_out);
                    timeToReachButton.startAnimation(popOut);
                    popOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            timeToReachButton.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
            break;
            case R.id.traffic_info_button: {
                mAnalytics.sendEventAnalytics("Traffic Info Button", "Tapped");
                if (googleMap.isTrafficEnabled()) {
                    googleMap.setTrafficEnabled(false);
                } else {
                    googleMap.setTrafficEnabled(true);
                }
            }
            break;
            case R.id.nearest_subway_button: {
                findNearestSubwayStation();
            }
            break;
            case R.id.get_me_home_button: {
                mAnalytics.sendEventAnalytics("Get Me Home Button", "Tapped");
                if (mSharedPreference.getUserHomeLocation() == null) {
                    Intent setUserHomeIntent = new Intent(getContext(), SetUserPreferenceLocationActivity.class);
                    setUserHomeIntent.putExtra("title", "Set Home");
                    startActivity(setUserHomeIntent);
                }
            }
            break;
            case R.id.get_me_to_work_button: {
                mAnalytics.sendEventAnalytics("Get Me to Work Button", "Tapped");
                if (mSharedPreference.getUserWorkLocation() == null) {
                    Intent setUserHomeIntent = new Intent(getContext(), SetUserPreferenceLocationActivity.class);
                    setUserHomeIntent.putExtra("title", "Set Work");
                    startActivity(setUserHomeIntent);
                }
            }
            break;
            case R.id.user_home_delete: {
                mAnalytics.sendEventAnalytics("Home Location Delete Button", "Tapped");

                mSharedPreference.clearUserHomeLocation();

                if (homeMarker != null) {
                    homeMarker.remove();
                    homeMarker = null;
                }

                onResume();
            }
            break;
            case R.id.user_work_delete: {
                mAnalytics.sendEventAnalytics("Work Location Button", "Tapped");
                mSharedPreference.clearUserWorkLocation();

                if (workMarker != null) {
                    workMarker.remove();
                    workMarker = null;
                }

                onResume();
            }
            break;
            case R.id.line_search_layout: {
                mAnalytics.sendEventAnalytics("Find a Line Button", "Tapped");
                Intent searchLineIntent = new Intent(getContext(), LineSearchActivity.class);
                startActivity(searchLineIntent);
            }
            break;
            case R.id.mock_location_indicator_layout: {
                mSharedPreference.setUserMockLocationMode(false);
                mSharedPreference.setUserMockLocation(0, 0);

                try {
                    mFusedLocationProviderClient.setMockMode(false);
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                }

                mockLocationIndicatorLayout.setVisibility(View.GONE);

                Intent mockLocationIntent = new Intent(getContext(), UserMockLocationSetActivity.class);
                startActivity(mockLocationIntent);

                getActivity().finish();
            }
            break;
            case R.id.sheet_drag_arrow: {
                if (appMenuSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    appMenuSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else if (appMenuSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    appMenuSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        }
    }

    private void drawStationsMarkers() {
        try {
            InputStreamReader is = null;
            try {
                is = new InputStreamReader(getContext().getAssets().open("station_locations.csv"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(is);

            String stationsData = "";

            while ((stationsData = reader.readLine()) != null) {
                String[] line = stationsData.split(",");

                double latitude = Double.parseDouble(line[9]);
                double longitude = Double.parseDouble(line[10]);
                stationName = String.valueOf(line[5]);

                SubwayStationsMarker stationsMarker = new SubwayStationsMarker(latitude, longitude, stationName, BitmapDescriptorFactory.fromAsset("subway.png"));
                clusterManager.addItem(stationsMarker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getReachTime(double lat, double lng, final String stationName, final boolean isStationInfo) {
        String distanceURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude + "," + longitude + "&destination=" + lat + "," + lng + "&alternatives=false&key=" + getString(R.string.places_api_key);

        JsonObjectRequest distanceRequest = new JsonObjectRequest(Request.Method.GET, distanceURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response", response.toString());

                if (response.has("status")) {
                    try {
                        String responseStatus = response.getString("status");

                        if (responseStatus.equalsIgnoreCase("OK")) {
                            JSONArray directionArray = response.getJSONArray("routes");

                            JSONObject legsData = directionArray.getJSONObject(0);

                            JSONArray legsArray = legsData.getJSONArray("legs");

                            JSONObject legsAttributes = legsArray.getJSONObject(0);

                            JSONObject durationObject = legsAttributes.getJSONObject("duration");

                            reachTime = durationObject.getString("text");

                            if (timeToReachButton.getVisibility() == View.GONE) {
                                Animation expandIn = AnimationUtils.loadAnimation(getContext(), R.anim.pop_in);
                                timeToReachButton.setVisibility(View.VISIBLE);
                                timeToReachButton.startAnimation(expandIn);
                            }

                            timeToReachButton.setText(reachTime);

                            if (isStationInfo) {
                                new LoadStationInfoTask(stationName).execute();
                            }

                            JSONObject overviewPolylineObject = legsData.getJSONObject("overview_polyline");

                            String routeEncodedPolyLine = overviewPolylineObject.getString("points");

                            if (routeEncodedPolyLine != null) {
                                if (routeLine != null) {
                                    routeLine.remove();
                                }

                                decodedRoutePolyLine = PolyUtil.decode(routeEncodedPolyLine);

                                PatternItem patternItem = new Dot();
                                List<PatternItem> patternItems = new ArrayList<>();

                                patternItems.add(patternItem);

                                routeLine = googleMap.addPolyline(new PolylineOptions()
                                        .addAll(decodedRoutePolyLine)
                                        .color(Color.parseColor("#1F9C3F"))
                                        .width(20)
                                        .geodesic(false)
                                        .zIndex(10)
                                        .startCap(new RoundCap())
                                        .endCap(new RoundCap())
                                        .visible(true));
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
                Log.e("ResponseError", error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(distanceRequest);
    }

    private void searchPlace(String placeName) {
        placeName = placeName.replaceAll("\\s", "%20");

        String searchUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + placeName + "&location=40.67,-73.94&radius=10000&key=" + getString(R.string.places_api_key);

        JsonObjectRequest placeSearchRequest = new JsonObjectRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("SearchResult", response.toString());

                if (appMenuSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    appMenuSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

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

                                if (searchedMarker != null) {
                                    searchedMarker.remove();
                                }

                                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                                googleMap.animateCamera(cameraUpdate);

                                searchedMarker = googleMap.addMarker(new MarkerOptions()
                                        .anchor(0.5f, 0.5f)
                                        .position(latLng)
                                        .title(placeName));

                                getReachTime(latLng.latitude, latLng.longitude, placeName, false);
                            } else {
                                searchResultsFragment = new SearchResultsFragment(getContext(), googleMap, resultsArrayList);
                                searchResultsFragment.show(getActivity().getSupportFragmentManager(), "search_result_fragment");
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
                Log.e("Error", error.getMessage());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(placeSearchRequest);
    }

    private void drawHomeMarker() {
        if (mSharedPreference.getUserHomeLocation() != null) {
            if (homeMarker == null) {
                if (googleMap != null) {
                    homeMarker = googleMap.addMarker(new MarkerOptions()
                            .title("Home")
                            .position(mSharedPreference.getUserHomeLocation())
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromAsset("ic_home_location.png")));
                }
            }
        }
    }

    private void drawWorkMarker() {
        if (mSharedPreference.getUserWorkLocation() != null) {
            if (workMarker == null) {
                if (googleMap != null) {
                    workMarker = googleMap.addMarker(new MarkerOptions()
                            .title("Work")
                            .position(mSharedPreference.getUserWorkLocation())
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromAsset("ic_work_location.png")));
                }
            }
        }
    }

    private void findNearestSubwayStation() {
        String searchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&rankby=distance&types=subway_station&key=" + getString(R.string.places_api_key);

        JsonObjectRequest placeSearchRequest = new JsonObjectRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("SearchResult", response.toString());

                if (response.has("status")) {
                    try {
                        String status = response.getString("status");

                        if (status.equalsIgnoreCase("OK")) {
                            JSONArray resultArray = response.getJSONArray("results");

                            JSONObject resultData = resultArray.getJSONObject(0);

                            JSONObject geometryData = resultData.getJSONObject("geometry");

                            JSONObject locationData = geometryData.getJSONObject("location");

                            String lat = locationData.getString("lat");
                            String lng = locationData.getString("lng");

                            String name = resultData.getString("name");

                            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                            googleMap.animateCamera(cameraUpdate);

                            if (name.contains("station")) {
                                String[] tempStationName = name.split("station");

                                name = tempStationName[0].trim();
                            }

                            if (name.contains("Station")) {
                                String[] tempStationName = name.split("Station");

                                name = tempStationName[0].trim();
                            }

                            if (name.contains("subway")) {
                                String[] tempStationName = name.split("subway");

                                name = tempStationName[0].trim();
                            }

                            if (name.contains("Subway")) {
                                String[] tempStationName = name.split("Subway");

                                name = tempStationName[0].trim();
                            }

                            if (name.contains("Avenue")) {
                                name = name.replace("Avenue", "Av");
                            }

                            if (name.contains("avenue")) {
                                name = name.replace("avenue", "Av");
                            }

                            if (name.contains("Street")) {
                                name = name.replace("Street", "St");
                            }

                            if (name.contains("street")) {
                                name = name.replace("street", "St");
                            }

                            if (name.contains("-")) {
                                String[] tempStationName = name.split("-");
                                name = tempStationName[0].trim();
                            } else {
                                String[] tempStationName = name.split(" ");

                                if (tempStationName[1] != null) {
                                    name = tempStationName[0] + " " + tempStationName[1];
                                } else {
                                    name = tempStationName[0];
                                }
                            }

                            getReachTime(latLng.latitude, latLng.longitude, name, true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.getMessage());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(placeSearchRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (mSharedPreference.getUserHomeLocation() != null) {
            setUserHomeButton.setVisibility(View.GONE);

            userHomeLayout.setVisibility(View.VISIBLE);

            userHomeLocation.setText(mSharedPreference.getUserHomeLocationName());

            drawHomeMarker();
        } else {
            setUserHomeButton.setVisibility(View.VISIBLE);

            userHomeLayout.setVisibility(View.GONE);
        }

        if (mSharedPreference.getUserWorkLocation() != null) {
            setUserWorkButton.setVisibility(View.GONE);

            userWorkLayout.setVisibility(View.VISIBLE);

            userWorkLocation.setText(mSharedPreference.getUserWorkLocationName());

            drawWorkMarker();
        } else {
            setUserWorkButton.setVisibility(View.VISIBLE);

            userWorkLayout.setVisibility(View.GONE);
        }
    }

    private void animateBottomSheetArrows(float slideOffset) {
        sheetDragArrow.setRotation(slideOffset * 180);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapLoaded() {
        drawWorkMarker();

        drawHomeMarker();
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
                    if (searchedMarker != null) {
                        searchedMarker.remove();
                    }

                    LatLng latLng = new LatLng(Double.parseDouble(resultsArrayList.get(position).getLat()), Double.parseDouble(resultsArrayList.get(position).getLng()));

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                    googleMap.animateCamera(cameraUpdate);

                    searchedMarker = googleMap.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .position(latLng)
                            .title(resultsArrayList.get(position).getTitle())
                            .icon(BitmapDescriptorFactory.fromAsset("ic_searched_place.png")));
                }
            });
        }

        private void initializeView(View view) {
            searchResultsView = (ListView) view.findViewById(R.id.line_search_results_view);
        }
    }

    private class LoadStationInfoTask extends AsyncTask<Void, Void, Void> {
        private Dialog stationInfoDialog;

        private View view;

        private TextView stationTitleText, timeToReachText;

        private String stationTitle;

        public LoadStationInfoTask(String stationTitle) {
            this.stationTitle = stationTitle;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingBar.setVisibility(View.VISIBLE);

            stationInfoDialog = new Dialog(getContext());

            stationInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_dialog_station_info, null, false);

            stationInfoDialog.setCanceledOnTouchOutside(true);
            stationInfoDialog.setContentView(view);

            stationTitleText = (TextView) view.findViewById(R.id.station_name);
            timeToReachText = (TextView) view.findViewById(R.id.time_to_reach_text);

            outboundSubwayList = (ListView) view.findViewById(R.id.outbound_subway_list);
            inboundSubwayList = (ListView) view.findViewById(R.id.inbound_subway_list);

            outboundSubwaySchedule = new ArrayList<>();
            inboundSubwaySchedule = new ArrayList<>();

            stationTitleText.setText(stationTitle);
            timeToReachText.setText(reachTime);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            outboundSubwaySchedule = database.getStationSchedule(stationTitle, 0);
            inboundSubwaySchedule = database.getStationSchedule(stationTitle, 1);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            outboundSubwayAdapter = new OutboundSubwayAdapter(getContext(), outboundSubwaySchedule);
            inboundSubwayAdapter = new InboundSubwayAdapter(getContext(), inboundSubwaySchedule);

            outboundSubwayList.setAdapter(outboundSubwayAdapter);
            inboundSubwayList.setAdapter(inboundSubwayAdapter);

            outboundSubwayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAnalytics.sendEventAnalytics("Outbound Subway", "Tapped");
                    Intent lineDrawActivity = new Intent(getContext(), DrawLineRouteActivity.class);
                    lineDrawActivity.putExtra("station_name", stationTitle);
                    lineDrawActivity.putExtra("line", outboundSubwaySchedule.get(position).getRouteName());
                    lineDrawActivity.putExtra("color", database.getRouteColor(outboundSubwaySchedule.get(position).getRouteName()));
                    startActivity(lineDrawActivity);
                }
            });

            inboundSubwayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAnalytics.sendEventAnalytics("Inbound Subway", "Tapped");
                    Intent lineDrawActivity = new Intent(getContext(), DrawLineRouteActivity.class);
                    lineDrawActivity.putExtra("station_name", stationTitle);
                    lineDrawActivity.putExtra("line", inboundSubwaySchedule.get(position).getRouteName());
                    lineDrawActivity.putExtra("color", database.getRouteColor(inboundSubwaySchedule.get(position).getRouteName()));
                    startActivity(lineDrawActivity);
                }
            });

            final CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    outboundSubwaySchedule.clear();
                    inboundSubwaySchedule.clear();

                    outboundSubwaySchedule = database.getStationSchedule(stationTitle, 0);
                    inboundSubwaySchedule = database.getStationSchedule(stationTitle, 1);

                    outboundSubwayAdapter = new OutboundSubwayAdapter(getContext(), outboundSubwaySchedule);
                    inboundSubwayAdapter = new InboundSubwayAdapter(getContext(), inboundSubwaySchedule);

                    outboundSubwayList.setAdapter(outboundSubwayAdapter);
                    inboundSubwayList.setAdapter(inboundSubwayAdapter);

                    this.start();
                }
            }.start();

            stationInfoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                }
            });

            stationInfoDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    loadingBar.setVisibility(View.GONE);
                }
            });

            stationInfoDialog.show();
        }
    }
}
