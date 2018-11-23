package com.maps.subwaytransit.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.maps.subwaytransit.BuildConfig;
import com.maps.subwaytransit.R;
import com.maps.subwaytransit.application.MainApplication;
import com.maps.subwaytransit.preference.SharedPreference;
import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserPermissionsRequestActivity extends AppCompatActivity {
    private Button permissionsGrantButton;

    private SharedPreference mSharedPreference;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private CountDownTimer countDownTimer;

    private Geocoder geocoder;

    private List<Address> currentLocation = new ArrayList<>();

    private String city;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_permission);

        mSharedPreference = new SharedPreference(this);

        geocoder = new Geocoder(this);

        mFusedLocationProviderClient = MainApplication.getFusedLocationProviderClient();

        initializeView();

        permissionsGrantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(UserPermissionsRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(UserPermissionsRequestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                } else {
                    mSharedPreference.setAllPermissionsGranted(true);
                    Intent mainIntent = new Intent(UserPermissionsRequestActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                }
            }
        });
    }

    private void initializeView() {
        permissionsGrantButton = (Button) findViewById(R.id.grant_permissions_button);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            boolean allGranted = false;

            int whichNotGranted[] = {2, 2};

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mSharedPreference.setAllPermissionsGranted(true);

                    mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(UserPermissionsRequestActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
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

                                final AlertDialog locationLoadDialog = new AlertDialog.Builder(UserPermissionsRequestActivity.this)
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
                                            Intent mainIntent = new Intent(UserPermissionsRequestActivity.this, MainActivity.class);
                                            startActivity(mainIntent);

                                            finish();
                                        } else {
                                            AlertDialog mockLocationDialog = new AlertDialog.Builder(UserPermissionsRequestActivity.this)
                                                    .setCancelable(false)
                                                    .setView(R.layout.layout_location_out_of_city)
                                                    .show();

                                            Button setMockLocationButton = mockLocationDialog.findViewById(R.id.mock_location_button);

                                            setMockLocationButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (isMockLocationEnabled()) {
                                                        Intent mockLocationIntent = new Intent(UserPermissionsRequestActivity.this, UserMockLocationSetActivity.class);
                                                        startActivity(mockLocationIntent);

                                                        finish();
                                                    } else {
                                                        AlertDialog enableMockLocationApp = new AlertDialog.Builder(UserPermissionsRequestActivity.this)
                                                                .setMessage("Please select this app as mock location provider in Settings -> Developer Options -> Select mock location app -> Select this app")
                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
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
                            } else {
                                Toast.makeText(UserPermissionsRequestActivity.this, "Failed to get current location, you will now be proceeded to select a mock location", Toast.LENGTH_LONG).show();


                                Intent mockLocationIntent = new Intent(UserPermissionsRequestActivity.this, UserMockLocationSetActivity.class);
                                startActivity(mockLocationIntent);

                                finish();
                            }
                        }
                    });
                } else {
                    checkLocationPermission();
                }
            }
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder storageReadPermissionDialog = new AlertDialog.Builder(this);

            storageReadPermissionDialog.setTitle("Fine Location Access Permission Required");
            storageReadPermissionDialog.setMessage("This app requires location access permission in order to access your location and optimize your transit experience.");
            storageReadPermissionDialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                }
            });

            storageReadPermissionDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog storageReadPermissionAlert = storageReadPermissionDialog.create();
            storageReadPermissionAlert.show();
        }
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
