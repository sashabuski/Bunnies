package com.HopIn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.HopIn.databinding.ActivityRiderMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class RiderMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRiderMapsBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private ClusterManager<CarClusterMarker> clusterManager;
    private CustomClusterRenderer clusterRenderer;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User currentUser;
    private UserLocation currentUserLocation;
    private BitmapDescriptor icon;
    private ArrayList<CarClusterMarker> mClusterMarkers = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRiderMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Intent intent = new Intent(this, ExitService.class);
        //startService(intent);//attempt to edit db on kill

        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        currentUser = (User) (getIntent().getSerializableExtra("loggedUser"));


        currentUserLocation = new UserLocation(currentUser);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        getDriversWithRealtimeUpdates(mMap, getCurrentFocus());
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                currentUserLocation.setGeoPoint(geoPoint);

                db.collection("Riders").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);


                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            }
        };

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    public void getDriversWithRealtimeUpdates(GoogleMap googleMap, View view) {

        if (clusterManager == null) {
            clusterManager = new ClusterManager<CarClusterMarker>(getApplicationContext(), mMap);
        }
        if (clusterRenderer == null) {
            clusterRenderer = new CustomClusterRenderer(
                    getApplicationContext(),
                    mMap,
                    clusterManager
            );
            clusterManager.setRenderer(clusterRenderer);
        }

        FirebaseFirestore.getInstance()
                .collection("Drivers")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {

                            return;
                        }
                        if (queryDocumentSnapshots != null) {

                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                            clusterManager.clearItems();
                            for (DocumentSnapshot snapshot : snapshotList) {
                                LatLng pls = new LatLng(snapshot.toObject(UserLocation.class).getGeoPoint().getLatitude(), snapshot.toObject(UserLocation.class).getGeoPoint().getLongitude());

                                CarClusterMarker ccm = new CarClusterMarker(pls.latitude,pls.longitude,snapshot.toObject(UserLocation.class).getUser().fName, "jkjkjk");

                                clusterManager.addItem(ccm);
                                mClusterMarkers.add(ccm);

                            }

                            clusterManager.cluster();


                        } else {
                            Log.e("Snapshot Error", "onEvent: query snapshot was null");
                        }
                    }
                });
    }
   /* @Override
    protected void onDestroy() {

        db.collection("Drivers").document(mAuth.getCurrentUser().getUid()).delete();
        super.onDestroy();//attempt to edit db on kill
    }*/
    @Override
    protected void onResume(){
        super.onResume();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                currentUserLocation.setGeoPoint(geoPoint);

                db.collection("Riders").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);


                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            }
        };
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {

        locationManager.removeUpdates(locationListener);
        db.collection("Riders").document(mAuth.getCurrentUser().getUid()).delete();
        super.onStop();
    }


    @Override
    public void onBackPressed(){//open prompt are you sure?
        db.collection("Riders").document(mAuth.getCurrentUser().getUid()).delete();
        locationManager.removeUpdates(locationListener);
        Intent intent = new Intent(this, PreScreen.class);
        startActivity(intent);


    }
}

