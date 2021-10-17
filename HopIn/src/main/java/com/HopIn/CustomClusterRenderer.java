package com.HopIn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * This is the Customised ClusterRenderer object. It is currently only used to display all the markers individually
 * (not as a cluster (dot) with number of markers displayed on it) by returning false in overriding the shouldRenderAsCluster method .
 *
 */

class CustomClusterRenderer<T extends CarClusterMarker> extends DefaultClusterRenderer<T>
{
    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {

        super(context, map, clusterManager);
    }


    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {

        return false;
    }

    @Override
    protected void onBeforeClusterItemRendered(CarClusterMarker item, MarkerOptions markerOptions) {

    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmarker));

    markerOptions.rotation(item.getUser().getBearing());
}
}

