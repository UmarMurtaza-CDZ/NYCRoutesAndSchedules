package com.maps.subwaytransit.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtimeNYCT;
import com.maps.subwaytransit.model.LineRouteModel;
import com.maps.subwaytransit.model.RouteModel;
import com.maps.subwaytransit.model.StationScheduleModel;
import com.maps.subwaytransit.preference.SharedPreference;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class GTFSFeedDatabase {
    private final Context context;

    // /////////////////////////// Column Names //////////////////////////////

    public String AGENCY_ID;
    public String AGENCY_NAME;
    public String AGENCY_URL;
    public String AGENCY_TIMEZONE;
    public String AGENCY_LANGUAGE;
    public String AGENCY_PHONE;

    public String ROUTE_ID;
    public String ROUTE_SHORT_NAME;
    public String ROUTE_LONG_NAME;
    public String ROUTE_DESC;
    public String ROUTE_TYPE;
    public String ROUTE_URL;
    public String ROUTE_COLOR;
    public String ROUTE_TEXT_COLOR;

    public String SHAPE_ID;
    public String SHAPE_PT_LAT;
    public String SHAPE_PT_LON;
    public String SHAPE_PT_SEQUENCE;
    public String SHAPE_DIST_TRAVELED;

    // ///////////////////////// Database And Table Name // ///////////////////////////

    private static final String DATABASE_NAME = "gtfs_feed_table.sqlite";
    private String TBL_AGENCY;
    private String TBL_ROUTES;
    private String TBL_SHAPES;
    private String TBL_TRIPS;

    // /////////////////////////// Table Create Queries // ////////////////////////////

    private static final int DATABASE_VERSION = 1;

    // /////////////////////// HELPER CLASS TO CREATE DATABASE // //////////////////////

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public GTFSFeedDatabase(Context context) {
        this.context = context;
        DBHelper = new DatabaseHelper(context);

        TBL_AGENCY = "agency";

        AGENCY_ID = "agency_id";
        AGENCY_NAME = "agency_name";
        AGENCY_URL = "agency_url";
        AGENCY_TIMEZONE = "agency_timezone";
        AGENCY_LANGUAGE = "agency_lang";
        AGENCY_PHONE = "agency_phone";

        TBL_ROUTES = "routes";

        ROUTE_ID = "route_id";
        ROUTE_SHORT_NAME = "route_short_name";
        ROUTE_LONG_NAME = "route_long_name";
        ROUTE_DESC = "route_desc";
        ROUTE_TYPE = "route_type";
        ROUTE_URL = "route_url";
        ROUTE_COLOR = "route_color";
        ROUTE_TEXT_COLOR = "route_text_color";

        TBL_SHAPES = "shapes";

        SHAPE_ID = "shape_id";
        SHAPE_PT_LAT = "shape_pt_lat";
        SHAPE_PT_LON = "shape_pt_lon";
        SHAPE_PT_SEQUENCE = "shape_pt_sequence";
        SHAPE_DIST_TRAVELED = "shapte_dist_traveled";

        TBL_TRIPS = "trips";
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    // /////////////////////////// Opens Database ////////////////////////////

    public GTFSFeedDatabase open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    // //////////////////////////// FUNCTIONS ////////////////////////////////

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */

    public void createDataBase() throws IOException {
        SharedPreference mSharedPreference = new SharedPreference(context);
        boolean dbExist = checkDataBase();
        int version = mSharedPreference.checkDbVersion();

        if (dbExist) {
            if (version <= DATABASE_VERSION) {
                copyDataBase();
                mSharedPreference.setDbVersion(DATABASE_VERSION + 1);
            } else if (DATABASE_VERSION > version) {
                mSharedPreference.setDbVersion(DATABASE_VERSION + 1);
            }
        } else {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            DBHelper.getReadableDatabase();

            try {
                copyDataBase();
                mSharedPreference.setDbVersion(DATABASE_VERSION + 1);
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        try {
            String myPath = context.getDatabasePath(DATABASE_NAME).getPath();

            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database does't exist yet.
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException {
        String DB_PATH = context.getDatabasePath(DATABASE_NAME).getPath();

        // Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    //Functions

    public ArrayList<RouteModel> getLineName(String lineName) {
        ArrayList<RouteModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getReadableDatabase();

        String query = "SELECT " + ROUTE_ID + ", " + ROUTE_SHORT_NAME + ", " + ROUTE_LONG_NAME + ", " + ROUTE_COLOR + " FROM " + TBL_ROUTES + " WHERE " + ROUTE_SHORT_NAME + " LIKE '" + lineName + "%'";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                RouteModel routeModel = new RouteModel();
                routeModel.setRouteId(cursor.getString(cursor.getColumnIndex(ROUTE_ID)));
                routeModel.setRouteShortName(cursor.getString(cursor.getColumnIndex(ROUTE_SHORT_NAME)));
                routeModel.setRouteLongName(cursor.getString(cursor.getColumnIndex(ROUTE_LONG_NAME)));
                routeModel.setRouteColor(cursor.getString(cursor.getColumnIndex(ROUTE_COLOR)));
                arrayList.add(routeModel);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return arrayList;
    }

    public ArrayList<RouteModel> getAllLines() {
        ArrayList<RouteModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + ROUTE_ID + ", " + ROUTE_SHORT_NAME + ", " + ROUTE_LONG_NAME + ", " + ROUTE_COLOR + " FROM " + TBL_ROUTES;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                RouteModel routeModel = new RouteModel();
                routeModel.setRouteId(cursor.getString(cursor.getColumnIndex(ROUTE_ID)));
                routeModel.setRouteShortName(cursor.getString(cursor.getColumnIndex(ROUTE_SHORT_NAME)));
                routeModel.setRouteLongName(cursor.getString(cursor.getColumnIndex(ROUTE_LONG_NAME)));
                routeModel.setRouteColor(cursor.getString(cursor.getColumnIndex(ROUTE_COLOR)));
                arrayList.add(routeModel);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return arrayList;
    }

    public ArrayList<LineRouteModel> getLineRoute(String lineName) {
        ArrayList<LineRouteModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getReadableDatabase();

        String query = "SELECT DISTINCT stops.stop_name, stops.stop_lat, stops.stop_lon, trips.shape_id " +
                "FROM trips " +
                "INNER JOIN stop_times ON stop_times.trip_id = trips.trip_id " +
                "INNER JOIN stops ON stops.stop_id = stop_times.stop_id " +
                "WHERE route_id = '" + lineName + "' GROUP BY stop_times.stop_sequence";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                LineRouteModel lineRouteModel = new LineRouteModel();
                lineRouteModel.setStopName(cursor.getString(cursor.getColumnIndex("stops.stop_name")));
                lineRouteModel.setShapeId(cursor.getString(cursor.getColumnIndex("trips.shape_id")));
                LatLng latLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex("stops.stop_lat"))), Double.parseDouble(cursor.getString(cursor.getColumnIndex("stops.stop_lon"))));
                lineRouteModel.setStopLatLng(latLng);
                arrayList.add(lineRouteModel);
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return arrayList;
    }

    public ArrayList<StationScheduleModel> getStationSchedule(String stationName, int direction) {
        ArrayList<StationScheduleModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getReadableDatabase();

        Date current = new Date();
        Date departure = null;

        String query = null;

        Calendar calendar = Calendar.getInstance();

        int weekOfDay = calendar.get(Calendar.DAY_OF_WEEK);

        switch (weekOfDay) {
            case Calendar.SUNDAY: {
                query = "SELECT trips.route_id, stop_times.stop_id, trips.trip_headsign, stop_times.arrival_time, stop_times.departure_time " +
                        "FROM stops " +
                        "INNER JOIN stop_times on stop_times.stop_id = stops.stop_id " +
                        "INNER JOIN trips ON trips.trip_id = stop_times.trip_id " +
                        "WHERE stops.stop_name LIKE '" + stationName + "%' AND trips.direction_id = " + direction + " AND trips.trip_id LIKE '%Sunday%'";
            }
            break;
            case Calendar.SATURDAY: {
                query = "SELECT trips.route_id, stop_times.stop_id, trips.trip_headsign, stop_times.arrival_time, stop_times.departure_time " +
                        "FROM stops " +
                        "INNER JOIN stop_times on stop_times.stop_id = stops.stop_id " +
                        "INNER JOIN trips ON trips.trip_id = stop_times.trip_id " +
                        "WHERE stops.stop_name LIKE '" + stationName + "%' AND trips.direction_id = " + direction + " AND trips.trip_id LIKE '%Saturday%'";
            }
            break;
            default: {
                query = "SELECT trips.route_id, stop_times.stop_id, trips.trip_headsign, stop_times.arrival_time, stop_times.departure_time " +
                        "FROM stops " +
                        "INNER JOIN stop_times on stop_times.stop_id = stops.stop_id " +
                        "INNER JOIN trips ON trips.trip_id = stop_times.trip_id " +
                        "WHERE stops.stop_name LIKE '" + stationName + "%' AND trips.direction_id = " + direction + " AND trips.trip_id LIKE '%Weekday%'";
            }
            break;
        }

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (direction == 1) {
                    String arrivalTime = cursor.getString(cursor.getColumnIndex("stop_times.arrival_time"));
                    String currentTime = "";

                    SimpleDateFormat currentTimeFormat = new SimpleDateFormat("E M d HH:mm:ss z y", Locale.getDefault());
                    SimpleDateFormat arrivalTimeFormat = new SimpleDateFormat("E M d HH:mm:ss z y", Locale.getDefault());

                    currentTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT-4"));

                    currentTime = currentTimeFormat.format(current);

                    try {
                        current = currentTimeFormat.parse(currentTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    StringTokenizer tokenizer = new StringTokenizer(currentTime);

                    String day = tokenizer.nextToken();
                    String month = tokenizer.nextToken();
                    String dayOfMonth = tokenizer.nextToken();
                    String time = tokenizer.nextToken();
                    String timeZone = tokenizer.nextToken();
                    String year = tokenizer.nextToken();

                    arrivalTime = day + " " + month + " " + dayOfMonth + " " + arrivalTime + " " + timeZone + " " + year;

                    try {
                        departure = arrivalTimeFormat.parse(arrivalTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

//                    new FeedLoaderTask(cursor).execute();

                    int days = 0, hours = 0, mins = 0;

                    if (current.before(departure)) {
                        long difference = departure.getTime() - current.getTime();
                        days = (int) (difference / (1000 * 60 * 60 * 24));
                        hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                        mins = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                    }

                    if (mins > 0 && mins <= 25) {
                        if (hours == 0) {
                            StationScheduleModel stationScheduleModel = new StationScheduleModel();
                            stationScheduleModel.setRouteName(cursor.getString(cursor.getColumnIndex("trips.route_id")));
                            stationScheduleModel.setStationName(cursor.getString(cursor.getColumnIndex("trips.trip_headsign")));
                            stationScheduleModel.setMins(String.valueOf(mins));
                            arrayList.add(stationScheduleModel);
                        }
                    }
                } else if (direction == 0) {
                    String departureTime = cursor.getString(cursor.getColumnIndex("stop_times.departure_time"));
                    String currentTime = "";

                    SimpleDateFormat currentTimeFormat = new SimpleDateFormat("E M d HH:mm:ss z y", Locale.getDefault());
                    SimpleDateFormat departureTimeFormat = new SimpleDateFormat("E M d HH:mm:ss z y", Locale.getDefault());

                    currentTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT-4"));

                    currentTime = currentTimeFormat.format(current);

                    try {
                        current = currentTimeFormat.parse(currentTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    StringTokenizer tokenizer = new StringTokenizer(currentTime);

                    String day = tokenizer.nextToken();
                    String month = tokenizer.nextToken();
                    String dayOfMonth = tokenizer.nextToken();
                    String time = tokenizer.nextToken();
                    String timeZone = tokenizer.nextToken();
                    String year = tokenizer.nextToken();

                    departureTime = day + " " + month + " " + dayOfMonth + " " + departureTime + " " + timeZone + " " + year;

                    try {
                        departure = departureTimeFormat.parse(departureTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    int days = 0, hours = 0, mins = 0;

                    if (current.before(departure)) {
                        long difference = departure.getTime() - current.getTime();
                        days = (int) (difference / (1000 * 60 * 60 * 24));
                        hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                        mins = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                    }

                    if (mins > 0 && mins <= 25) {
                        if (hours == 0) {
                            StationScheduleModel stationScheduleModel = new StationScheduleModel();
                            stationScheduleModel.setRouteName(cursor.getString(cursor.getColumnIndex("trips.route_id")));
                            stationScheduleModel.setStationName(cursor.getString(cursor.getColumnIndex("trips.trip_headsign")));
                            stationScheduleModel.setMins(String.valueOf(mins));
                            arrayList.add(stationScheduleModel);
                        }
                    }
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();

//        HashMap<String, ArrayList<StationScheduleModel>> map = new HashMap<>();

//        for (int i = 0; i < arrayList.size(); i++) {
//            if (map.containsKey(arrayList.get(i).getStationName())) {
////                map.get(arrayList.get(i).getStationName()).add(arrayList.get(i));
//            } else {
//                map.put(arrayList.get(i).getStationName(), arrayList);
//            }
//        }

        return arrayList;
    }

    public String getRouteColor(String routeId) {
        SQLiteDatabase db = DBHelper.getReadableDatabase();

        String routeColor = "";

        String query = "SELECT " + ROUTE_COLOR + " FROM " + TBL_ROUTES + " WHERE " + ROUTE_ID + " = '" + routeId + "'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                routeColor = cursor.getString(cursor.getColumnIndex(ROUTE_COLOR));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return routeColor;
    }

    private class FeedLoaderTask extends AsyncTask<Void, Void, Void> {
        private Cursor cursor;


        public FeedLoaderTask(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            URL url = null;

            try {
                url = new URL("http://datamine.mta.info/mta_esi.php?key=95352a22a8a65009da1af201702f9243&feed_id=26");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(context, "URL Error", Toast.LENGTH_SHORT).show();
            }

            GtfsRealtime.FeedMessage feed = null;

            try {
                ExtensionRegistry registry = ExtensionRegistry.newInstance();
                registry.add(GtfsRealtimeNYCT.nyctFeedHeader);
                registry.add(GtfsRealtimeNYCT.nyctStopTimeUpdate);
                registry.add(GtfsRealtimeNYCT.nyctTripDescriptor);

                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream(), registry);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasTripUpdate()) {
                    if (entity.getTripUpdate().getTrip().getRouteId().equalsIgnoreCase(cursor.getString(cursor.getColumnIndex("trips.route_id")))) {
                        List<GtfsRealtime.TripUpdate.StopTimeUpdate> list = entity.getTripUpdate().getStopTimeUpdateList();
                        for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : list) {
                            if (stopTimeUpdate.getStopId().equalsIgnoreCase(cursor.getString(cursor.getColumnIndex("stop_times.stop_id")))) {
                                Log.e("Time", "" + stopTimeUpdate.getDeparture().getTime());
                            }
                        }
                    }

                    if (entity.getTripUpdate().hasDelay()) {
                        Log.e("TimeDelay", "" + entity.getTripUpdate().getDelay());
                    }
                }
            }

            return null;
        }
    }
}
