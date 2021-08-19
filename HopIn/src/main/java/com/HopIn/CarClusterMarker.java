package com.HopIn;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * This is the Custom ClusterItem class. It is used to create clusterItems(like advanced map markers) that contain
 * the corresponding UserLocation(contains User) object.
 * They are added to the clusterManager and displayed on the Rider Map activity as live moving car markers
 * that can be clicked by the user to request a ride.
 *
 */

public class CarClusterMarker implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;
    private final UserLocation user;

    //adduserlocation
    public CarClusterMarker(double lat, double lng, String title, String snippet,  UserLocation user) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.user = user;
    }

    public UserLocation getUser() {
        return user;
    }
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
