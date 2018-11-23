package com.maps.subwaytransit.utils;

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class SubwayStationsMarker implements ClusterItem {
    private final LatLng mPosition;

    private String mTitle;

    private BitmapDescriptor mIcon;

    public SubwayStationsMarker(double lat, double lng, String title, BitmapDescriptor icon) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mIcon = icon;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() { return mTitle; }

    public BitmapDescriptor getIcon() {
        return mIcon;
    }

    /**
     * Set the title of the marker
     * @param title string to be set as title
     */

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setIcon(BitmapDescriptor mIcon) {
        this.mIcon = mIcon;
    }
}