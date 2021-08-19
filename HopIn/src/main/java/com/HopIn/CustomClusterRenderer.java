package com.HopIn;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * This is the Customised ClusterRenderer object. It is currently only used to display all the markers individually
 * (not as a cluster (dot) with number of markers displayed on it) by returning false in overriding the shouldRenderAsCluster method .
 *
 */
class CustomClusterRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T>
{
    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        //start clustering if at least 2 items overlap
        //Change your logic here
        return false;
    }
}

