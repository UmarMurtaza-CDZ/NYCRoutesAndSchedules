package com.maps.subwaytransit.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.android.gms.maps.model.LatLng;

public class SharedPreference {
    private SharedPreferences sharedPreferences;

    private Editor editor;

    private Context context;

    private int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "transit_preference";
    public static final String DBVERSION = "db_version";

    public static final String PERMISSIONS_GRANTED = "permissions_grant";
    public static final String FIRST_RUN = "first_run";
    public static final String USER_HOME_LAT = "user_home_lat";
    public static final String USER_HOME_LNG = "user_home_lng";
    public static final String USER_HOME_NAME = "user_home_name";
    public static final String USER_WORK_LAT = "user_work_lat";
    public static final String USER_WORK_LNG = "user_work_long";
    public static final String USER_WORK_NAME = "user_work_name";
    public static final String USER_MOCK_ACTIVE = "user_mock_active";
    public static final String USER_MOCK_LAT = "user_mock_lat";
    public static final String USER_MOCK_LNG = "user_mock_long";

    private static final String USER_FEEDBACK = "user_feedback";


    public SharedPreference(Context context) {
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void setDbVersion(int v) {
        editor.putInt(DBVERSION, v);
        editor.commit();
    }

    public int checkDbVersion() {
        return sharedPreferences.getInt(DBVERSION, 1);
    }

    public void setAllPermissionsGranted(Boolean granted) {
        editor.putBoolean(PERMISSIONS_GRANTED, granted);
        editor.commit();
    }

    public Boolean getAllPermissionsGranted() {
        return sharedPreferences.getBoolean(PERMISSIONS_GRANTED, false);
    }

    public void setFirstRun(Boolean firstRun) {
        editor.putBoolean(FIRST_RUN, firstRun);
        editor.commit();
    }

    public Boolean isFirstRun() {
        return sharedPreferences.getBoolean(FIRST_RUN, true);
    }

    public void setUserHomeLocation(double lat, double lng) {
        editor.putLong(USER_HOME_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(USER_HOME_LNG, Double.doubleToRawLongBits(lng));
        editor.commit();
    }

    public void clearUserHomeLocation() {
        editor.remove(USER_HOME_LAT);
        editor.remove(USER_HOME_LNG);
        editor.remove(USER_HOME_NAME);
        editor.commit();
    }

    public LatLng getUserHomeLocation() {
        if (sharedPreferences.getLong(USER_HOME_LAT, 0) == 0 && sharedPreferences.getLong(USER_HOME_LNG, 0) == 0) {
            return null;
        } else {
            double lat = Double.longBitsToDouble(sharedPreferences.getLong(USER_HOME_LAT, 0));
            double lng = Double.longBitsToDouble(sharedPreferences.getLong(USER_HOME_LNG, 0));

            return new LatLng(lat, lng);
        }
    }

    public void setUserHomeLocationName(String name) {
        editor.putString(USER_HOME_NAME, name);
        editor.commit();
    }

    public String getUserHomeLocationName() {
        return sharedPreferences.getString(USER_HOME_NAME, "");
    }

    public void setUserWorkLocation(double lat, double lng) {
        editor.putLong(USER_WORK_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(USER_WORK_LNG, Double.doubleToRawLongBits(lng));
        editor.commit();
    }

    public void clearUserWorkLocation() {
        editor.remove(USER_WORK_LAT);
        editor.remove(USER_WORK_LNG);
        editor.remove(USER_WORK_NAME);
        editor.commit();
    }

    public LatLng getUserWorkLocation() {
        if (sharedPreferences.getLong(USER_WORK_LAT, 0) == 0 && sharedPreferences.getLong(USER_WORK_LNG, 0) == 0) {
            return null;
        } else {
            double lat = Double.longBitsToDouble(sharedPreferences.getLong(USER_WORK_LAT, 0));
            double lng = Double.longBitsToDouble(sharedPreferences.getLong(USER_WORK_LNG, 0));

            return new LatLng(lat, lng);
        }
    }

    public void setUserWorkLocationName(String name) {
        editor.putString(USER_WORK_NAME, name);
        editor.commit();
    }

    public String getUserWorkLocationName() {
        return sharedPreferences.getString(USER_WORK_NAME, "");
    }

    public void setUserFeedback(boolean feedback) {
        editor.putBoolean(USER_FEEDBACK, feedback);
        editor.commit();
    }

    public boolean getUserFeedback() {
        return sharedPreferences.getBoolean(USER_FEEDBACK, false);
    }

    public void setUserMockLocationMode(boolean active) {
        editor.putBoolean(USER_MOCK_ACTIVE, active);
        editor.commit();
    }

    public boolean getUserMockLocationMode() {
        return sharedPreferences.getBoolean(USER_MOCK_ACTIVE, false);
    }

    public void setUserMockLocation(double lat, double lng) {
        editor.putLong(USER_MOCK_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(USER_MOCK_LNG, Double.doubleToRawLongBits(lng));
        editor.commit();
    }

    public LatLng getUserMockLocation() {
        if (sharedPreferences.getLong(USER_MOCK_LAT, 0) == 0 && sharedPreferences.getLong(USER_MOCK_LNG, 0) == 0) {
            return null;
        } else {
            double lat = Double.longBitsToDouble(sharedPreferences.getLong(USER_MOCK_LAT, 0));
            double lng = Double.longBitsToDouble(sharedPreferences.getLong(USER_MOCK_LNG, 0));

            if (lat == 0 && lng == 0) {
                return null;
            } else {
                return new LatLng(lat, lng);
            }
        }
    }
}