package com.HopIn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
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
import com.HopIn.databinding.ActivityDriverMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.List;

/**
 * This Map activity is seen by the drivers. It also sends realtime updates of the drivers geopoint
 * to the database "drivers" table.
 *
 * ---Need to fix when location updates are turned off + remove document from "drivers" DB collection.
 * ---(onStop, onResume, onDestroy)
 */

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityDriverMapsBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User currentUser;
    private UserLocation currentUserLocation;
    private BitmapDescriptor icon;
    private boolean requested = false;
    private ConstraintLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        currentUser = (User) (getIntent().getSerializableExtra("loggedUser"));
        currentUserLocation = new UserLocation(currentUser);
        cl = findViewById(R.id.constraintLayout);
        cl.setVisibility(View.GONE);

    }

    /**
     * Implements a locationListener that sends real time geopoint updates to the driver DB collection
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        listenForRequests(mMap );

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
                currentUserLocation.setBearing(location.getBearing());
                currentUserLocation.setGeoPoint(geoPoint);
                currentUserLocation.setTimestamp(null);

                db.collection("Drivers").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);
                if(!requested) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }
        };

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 2, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
public void listenForRequests(GoogleMap googleMap){
    FirebaseFirestore.getInstance()
            .collection("Rides")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (e != null) {

                        return;
                    }
                    if (queryDocumentSnapshots != null) {

                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot snapshot : snapshotList) {
                            Ride newRide = snapshot.toObject(Ride.class);

                            if(newRide.getDriver().getUser().email != null && currentUser.email != null) {

                                if (newRide.getDriver().getUser().email.equals(currentUser.email)) {

                                    if (newRide.getStatus().equals("REQUESTED")) {

                                        if (isNewRequest(newRide)) {
                                            //open request pop up
                                            requested = true;
                                            LatLng l = new LatLng(newRide.getRider().getGeoPoint().getLatitude(),newRide.getRider().getGeoPoint().getLongitude());
                                            googleMap.addMarker(new MarkerOptions().position(l).title("yuck"));
                                            CameraUpdate center = CameraUpdateFactory.newLatLng(l);
                                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                                            googleMap.moveCamera(center);
                                            googleMap.animateCamera(zoom);



                                           /* BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DriverMapsActivity.this, R.style.ResponseDialog);
                                            View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_response_sheet, (LinearLayout)findViewById(R.id.responseSheetContainer));
                                            TextView a = bottomSheetView.findViewById(R.id.name);
                                            a.setText(newRide.getRider().getUser().fName);
                                            bottomSheetDialog.setContentView(bottomSheetView);
                                            Button acceptButton = (Button)bottomSheetDialog.findViewById(R.id.acceptButton);
                                            Button declineButton = (Button)bottomSheetDialog.findViewById(R.id.declineButton);
                                            bottomSheetDialog.setCancelable(false);*/
                                            Button acceptButton = (Button)findViewById(R.id.acceptButton);
                                            Button declineButton = (Button)findViewById(R.id.declineButton);
                                            TextView a = findViewById(R.id.textView);
                                            a.setText(newRide.getRider().getUser().fName+" "+newRide.getRider().getUser().lName);
                                            acceptButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                db.collection("Rides").document(snapshot.getId()).update("status", "ACCEPTED");
                                                    System.out.println(snapshot.getId());
                                                    cl.setVisibility(View.GONE);
                                                   LatLng a = new LatLng(currentUserLocation.getGeoPoint().getLatitude(),currentUserLocation.getGeoPoint().getLongitude());
                                                    CameraUpdate center = CameraUpdateFactory.newLatLng(a);
                                                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                                                    //start route
                                                }
                                            });

                                            declineButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    db.collection("Rides").document(snapshot.getId()).update("status", "DECLINED");
                                                    requested = false;
                                                    googleMap.clear();
                                                    cl.setVisibility(View.GONE);
                                                }
                                            });

                                            cl.setVisibility(View.VISIBLE);


                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            });
    }

public boolean isNewRequest(Ride newRide){

                    Date liveTime = new Date(System.currentTimeMillis()- 5000);
    if (newRide.getTimestamp() != null) {
        if (newRide.getTimestamp().after(liveTime)) {
            return true;
        } else {
            return false;
        }
    }
    return false;

}




    @Override
    public void onBackPressed(){//open prompt are you sure?
        locationManager.removeUpdates(locationListener);
        db.collection("Drivers").document(mAuth.getCurrentUser().getUid()).delete();
        Intent intent = new Intent(this, PreScreen.class);
        startActivity(intent);


    }
}
