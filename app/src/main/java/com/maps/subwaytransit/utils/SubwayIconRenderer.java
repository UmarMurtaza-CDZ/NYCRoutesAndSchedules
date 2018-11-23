package com.maps.subwaytransit.utils;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class SubwayIconRenderer extends DefaultClusterRenderer<SubwayStationsMarker> {

    public SubwayIconRenderer(Context context, GoogleMap map, ClusterManager<SubwayStationsMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(SubwayStationsMarker stationsMarker, MarkerOptions markerOptions) {
        markerOptions.icon(stationsMarker.getIcon());
        markerOptions.title(stationsMarker.getTitle());
        markerOptions.anchor(0.5f, 0.5f);
        super.onBeforeClusterItemRendered(stationsMarker, markerOptions);
    }
}
