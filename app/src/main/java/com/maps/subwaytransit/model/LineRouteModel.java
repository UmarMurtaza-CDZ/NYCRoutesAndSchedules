package com.maps.subwaytransit.model;

import com.google.android.gms.maps.model.LatLng;

public class LineRouteModel {
    private String stopName, shapeId;

    private LatLng stopLatLng;

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    public LatLng getStopLatLng() {
        return stopLatLng;
    }

    public void setStopLatLng(LatLng stopLatLng) {
        this.stopLatLng = stopLatLng;
    }
}
