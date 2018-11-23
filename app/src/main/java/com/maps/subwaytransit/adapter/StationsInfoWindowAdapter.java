package com.maps.subwaytransit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.maps.subwaytransit.R;

public class StationsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;

    private View view;

    public StationsInfoWindowAdapter(Context context) {
        this.context = context;

        view = LayoutInflater.from(context).inflate(R.layout.layout_custom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView stationName = (TextView) view.findViewById(R.id.station_name);

        stationName.setText(marker.getTitle());

        return view;
    }
}
