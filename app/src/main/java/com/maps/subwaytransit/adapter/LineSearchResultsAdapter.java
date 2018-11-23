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
import com.maps.subwaytransit.model.RouteModel;

import java.util.ArrayList;

public class LineSearchResultsAdapter extends ArrayAdapter<RouteModel> {
    private Context context;

    private ArrayList<RouteModel> routeModelArrayList = new ArrayList<>();

    public LineSearchResultsAdapter(Context context, ArrayList<RouteModel> routeModelArrayList) {
        super(context, 0, routeModelArrayList);
        this.context = context;
        this.routeModelArrayList = routeModelArrayList;
    }

    @NonNull
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_line_search_result, parent, false);

        ImageView lineLetter = (ImageView) view.findViewById(R.id.line_letter);

        TextView lineName = (TextView) view.findViewById(R.id.line_name);

        String resourceString = "route_" + routeModelArrayList.get(position).getRouteShortName().toLowerCase();

        if (resourceString.equals("route_5x")) {
            resourceString = "route_5";
        }

        int resourceId = context.getResources().getIdentifier(resourceString, "drawable", context.getPackageName());

        if (resourceId != 0) {
            lineLetter.setImageResource(resourceId);
        } else {
            lineLetter.setImageResource(0);
        }

        lineName.setText(routeModelArrayList.get(position).getRouteLongName());

        return view;
    }

    @Override
    public int getCount() {
        return routeModelArrayList.size();
    }
}
