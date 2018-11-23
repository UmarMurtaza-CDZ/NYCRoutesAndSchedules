package com.maps.subwaytransit.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maps.subwaytransit.R;

import java.util.ArrayList;

public class LineRouteStationsAdapter extends ArrayAdapter<String> {
    private Context context;

    private ArrayList<String> lineRouteStationsList = new ArrayList<>();

    private String desiredStation;

    public LineRouteStationsAdapter(Context context, ArrayList<String> lineRouteStationsList, String desiredStation) {
        super(context, 0, lineRouteStationsList);
        this.context = context;
        this.lineRouteStationsList = lineRouteStationsList;
        this.desiredStation = desiredStation;
    }

    @NonNull
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_line_route_station, parent, false);

        TextView lineName = (TextView) view.findViewById(R.id.station_name);

            lineName.setText(lineRouteStationsList.get(position));

        if (lineRouteStationsList.get(position).equals(desiredStation)) {
            Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
            lineName.setTypeface(boldTypeface);
        }

        return view;
    }

    @Override
    public int getCount() {
        return lineRouteStationsList.size();
    }
}
