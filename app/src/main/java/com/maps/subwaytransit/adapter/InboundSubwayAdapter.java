package com.maps.subwaytransit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maps.subwaytransit.R;
import com.maps.subwaytransit.model.StationScheduleModel;

import java.util.ArrayList;

public class InboundSubwayAdapter extends ArrayAdapter<StationScheduleModel> {
    private Context context;

    private ArrayList<StationScheduleModel> subwayDestinationName = new ArrayList<>();

    public InboundSubwayAdapter(Context context, ArrayList<StationScheduleModel> subwayDestinationName) {
        super(context, 0, subwayDestinationName);
        this.context = context;
        this.subwayDestinationName = subwayDestinationName;
    }

    @NonNull
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_line_destination_station_name, parent, false);

        ImageView routeName = (ImageView) view.findViewById(R.id.route_name);

        TextView lineName = (TextView) view.findViewById(R.id.line_name);
        TextView timeLeft = (TextView) view.findViewById(R.id.time_left);

        String resourceString = "route_" + subwayDestinationName.get(position).getRouteName().toLowerCase();

        if (resourceString.equals("route_5x")) {
            resourceString = "route_5";
        } else if (resourceString.equals("route_fs")) {
            resourceString = "route_s";
        } else if (resourceString.equals("route_h")) {
            resourceString = "route_s";
        }

        int resourceId = context.getResources().getIdentifier(resourceString, "drawable", context.getPackageName());

        if (resourceId != 0) {
            routeName.setImageResource(resourceId);
        } else {
            routeName.setImageResource(0);
        }

        lineName.setText(subwayDestinationName.get(position).getStationName());
        timeLeft.setText(subwayDestinationName.get(position).getMins() + " mins");

        return view;
    }

    @Override
    public int getCount() {
        return subwayDestinationName.size();
    }
}
