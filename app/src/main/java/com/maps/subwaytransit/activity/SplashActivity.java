package com.maps.subwaytransit.activity;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.maps.subwaytransit.BuildConfig;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.application.MainApplication;
import com.maps.subwaytransit.database.GTFSFeedDatabase;
import com.maps.subwaytransit.preference.SharedPreference;
import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME = 5000;

    private SharedPreference mSharedPreference;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private CountDownTimer countDownTimer;

    private Geocoder geocoder;

    private List<Address> currentLocation = new ArrayList<>();

    private String city = "lame";
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        GTFSFeedDatabase database = new GTFSFeedDatabase(this);
        try {
            database.createDataBase();
            database.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSharedPreference = new SharedPreference(this);

        geocoder = new Geocoder(this);

        mFusedLocationProviderClient = MainApplication.getFusedLocationProviderClient();

        try {
            mFusedLocationProviderClient.setMockMode(false);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!mSharedPreference.getAllPermissionsGranted()) {
                        Intent permissionsIntent = new Intent(SplashActivity.this, UserPermissionsRequestActivity.class);
                        startActivity(permissionsIntent);

                        finish();
                    } else {
                        if (mSharedPreference.getUserMockLocationMode() && mSharedPreference.getUserMockLocation() != null) {
                            Intent homeIntent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(homeIntent);

                            finish();
                        } else {
                            try {
                                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(SplashActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        try {
                                            latitude = location.getLatitude();
                                            longitude = location.getLongitude();

                                            try {
                                                currentLocation = geocoder.getFromLocation(latitude, longitude, 1);

                                                city = currentLocation.get(0).getAdminArea();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        } catch (NullPointerException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                final AlertDialog locationLoadDialog = new AlertDialog.Builder(SplashActivity.this)
                                        .setCancelable(false)
                                        .setView(R.layout.layout_location_detect_dialog)
                                        .show();

                                final RotateLoading progressBar = locationLoadDialog.findViewById(R.id.progress_bar);
                                progressBar.start();

                                locationLoadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        progressBar.stop();
                                    }
                                });

                                countDownTimer = new CountDownTimer(3000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        locationLoadDialog.dismiss();

                                        if (city.equals("New York")) {
                                            Intent homeIntent = new Intent(SplashActivity.this, MainActivity.class);
                                            startActivity(homeIntent);

                                            finish();
                                        } else {
                                            AlertDialog mockLocationDialog = new AlertDialog.Builder(SplashActivity.this)
                                                    .setCancelable(false)
                                                    .setView(R.layout.layout_location_out_of_city)
                                                    .show();

                                            Button setMockLocationButton = mockLocationDialog.findViewById(R.id.mock_location_button);

                                            setMockLocationButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (isMockLocationEnabled()) {
                                                        Intent mockLocationIntent = new Intent(SplashActivity.this, UserMockLocationSetActivity.class);
                                                        startActivity(mockLocationIntent);

                                                        finish();
                                                    } else {
                                                        AlertDialog enableMockLocationApp = new AlertDialog.Builder(SplashActivity.this)
                                                                .setMessage("Please select this app as mock location provider in Settings -> Developer Options -> Select mock location app -> Select this app")
                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));

                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                };

                                countDownTimer.start();
                            } catch (SecurityException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } else {
                    if (mSharedPreference.getUserMockLocationMode() && mSharedPreference.getUserMockLocation() != null) {
                        Intent homeIntent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(homeIntent);

                        finish();
                    } else {
                        try {
                            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(SplashActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    try {

                                        if (location != null) {
                                            latitude = location.getLatitude();
                                            longitude = location.getLongitude();

                                            try {
                                                currentLocation = geocoder.getFromLocation(latitude, longitude, 1);

                                                city = currentLocation.get(0).getAdminArea();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            final AlertDialog locationLoadDialog = new AlertDialog.Builder(SplashActivity.this)
                                                    .setCancelable(false)
                                                    .setView(R.layout.layout_location_detect_dialog)
                                                    .show();

                                            final RotateLoading progressBar = locationLoadDialog.findViewById(R.id.progress_bar);
                                            progressBar.start();

                                            locationLoadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    progressBar.stop();
                                                }
                                            });

                                            countDownTimer = new CountDownTimer(3000, 1000) {
                                                @Override
                                                public void onTick(long millisUntilFinished) {

                                                }

                                                @Override
                                                public void onFinish() {
                                                    locationLoadDialog.dismiss();

                                                    if (city.equals("New York")) {
                                                        Intent homeIntent = new Intent(SplashActivity.this, MainActivity.class);
                                                        startActivity(homeIntent);

                                                        finish();
                                                    } else {
                                                        AlertDialog mockLocationDialog = new AlertDialog.Builder(SplashActivity.this)
                                                                .setCancelable(false)
                                                                .setView(R.layout.layout_location_out_of_city)
                                                                .show();

                                                        Button setMockLocationButton = mockLocationDialog.findViewById(R.id.mock_location_button);

                                                        setMockLocationButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (isMockLocationEnabled()) {
                                                                    Intent mockLocationIntent = new Intent(SplashActivity.this, UserMockLocationSetActivity.class);
                                                                    startActivity(mockLocationIntent);

                                                                    finish();
                                                                } else {
                                                                    AlertDialog enableMockLocationApp = new AlertDialog.Builder(SplashActivity.this)
                                                                            .setMessage("Please select this app as mock location provider in Settings -> Developer Options -> Select mock location app -> Select this app")
                                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));

                                                                                    dialog.dismiss();
                                                                                }
                                                                            }).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            };

                                            countDownTimer.start();
                                        } else {
                                            Toast.makeText(SplashActivity.this, "Failed to get current location, you will now be proceeded to select a mock location", Toast.LENGTH_LONG).show();

                                            Intent mockLocationIntent = new Intent(SplashActivity.this, UserMockLocationSetActivity.class);
                                            startActivity(mockLocationIntent);

                                            finish();
                                        }
                                    } catch (NullPointerException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                        } catch (SecurityException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }, SPLASH_TIME);
    }

    private boolean isMockLocationEnabled() {
        boolean isMockLocation = false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                isMockLocation = !android.provider.Settings.Secure.getString(getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }

        return isMockLocation;
    }
}
