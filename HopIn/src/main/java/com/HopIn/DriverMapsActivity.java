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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

import org.jetbrains.annotations.NotNull;

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
    private FusedLocationProviderClient fusedLocation;
    private Button declineBut;
    private   Button acceptBut;
    private View bottomSheetView;
    private BottomSheetBehavior bottomSheetBehavior;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bottomSheetView = (View)findViewById(R.id.driverBottomSheet);
        if (bottomSheetView == null)
        {
            System.out.println("LALALLALALALALLALLLALA");
        }
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        currentUser = (User) (getIntent().getSerializableExtra("loggedUser"));
        currentUserLocation = new UserLocation(currentUser);

        fusedLocation = LocationServices.getFusedLocationProviderClient(DriverMapsActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                GeoPoint geoPoint = new GeoPoint(task.getResult().getLatitude(), task.getResult().getLongitude());
                currentUserLocation.setBearing(task.getResult().getBearing());
                currentUserLocation.setGeoPoint(geoPoint);
                currentUserLocation.setTimestamp(null);
                db.collection("Drivers").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);
            }
        });
        //cl = findViewById(R.id.constraintLayout);
        //  cl.setVisibility(View.GONE);
           declineBut = findViewById(R.id.declineButton);
           acceptBut = findViewById(R.id.acceptButton);
           declineBut.setVisibility(View.GONE);
           acceptBut.setVisibility(View.GONE);
           findViewById(R.id.requestText).setVisibility(View.GONE);
           findViewById(R.id.onTheWay).setVisibility(View.GONE);
           findViewById(R.id.arrivedButton).setVisibility(View.GONE);
           findViewById(R.id.arriveTip).setVisibility(View.GONE);
           findViewById(R.id.requestPic).setVisibility(View.GONE);

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
        updateAndZoomLocation(mMap, fusedLocation);


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
                                        System.out.println("huhuhuhuhuhuhuhuhuhuhuhuhuhuhuhuhuuhuhhuhuhu");
                                        if (isNewRequest(newRide)) {
                                            //open request pop up
                                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
                                            Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);

                                            findViewById(R.id.requestPic).startAnimation(animFadeIn);
                                            findViewById(R.id.requestPic).setVisibility(View.VISIBLE);
                                            acceptBut.startAnimation(animFadeIn);
                                            acceptBut.setVisibility(View.VISIBLE);
                                            declineBut.startAnimation(animFadeIn);
                                            declineBut.setVisibility(View.VISIBLE);
                                            TextView requestText = findViewById(R.id.requestText);
                                            requestText.setText("New ride request from "+newRide.getRider().getUser().fName+"!");
                                            requestText.startAnimation(animFadeIn);
                                            requestText.setVisibility(View.VISIBLE);
                                            acceptBut.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                db.collection("Rides").document(snapshot.getId()).update("status", "ACCEPTED");
                                                    System.out.println(snapshot.getId());

                                                   LatLng a = new LatLng(currentUserLocation.getGeoPoint().getLatitude(),currentUserLocation.getGeoPoint().getLongitude());
                                                    findViewById(R.id.requestPic).startAnimation(animFadeOut);
                                                    findViewById(R.id.requestPic).setVisibility(View.GONE);
                                                    requestText.startAnimation(animFadeOut);
                                                    requestText.setVisibility(View.GONE);
                                                    acceptBut.startAnimation(animFadeOut);
                                                    acceptBut.setVisibility(View.GONE);
                                                    declineBut.startAnimation(animFadeOut);
                                                    declineBut.setVisibility(View.GONE);
                                                    CameraUpdate center = CameraUpdateFactory.newLatLng(a);
                                                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);


                                                    findViewById(R.id.arrivedButton).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            db.collection("Rides").document(snapshot.getId()).update("status", "ARRIVED");

                                                            findViewById(R.id.arriveTip).startAnimation(animFadeOut);
                                                            findViewById(R.id.arriveTip).setVisibility(View.GONE);
                                                            findViewById(R.id.arrivedButton).startAnimation(animFadeOut);
                                                            findViewById(R.id.arrivedButton).setVisibility(View.GONE);

                                                            requestText.startAnimation(animFadeOut);
                                                            requestText.setVisibility(View.GONE);
                                                            findViewById(R.id.onTheWay).startAnimation(animFadeOut);
                                                            findViewById(R.id.onTheWay).setVisibility(View.GONE);

                                                        }
                                                    });


                                                    findViewById(R.id.arriveTip).startAnimation(animFadeIn);
                                                    findViewById(R.id.arriveTip).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.arrivedButton).startAnimation(animFadeIn);
                                                    findViewById(R.id.arrivedButton).setVisibility(View.VISIBLE);
                                                    requestText.setText("On route to "+newRide.getRider().getUser().fName+"!");
                                                    requestText.startAnimation(animFadeIn);
                                                    requestText.setVisibility(View.VISIBLE);
                                                    findViewById(R.id.onTheWay).startAnimation(animFadeIn);
                                                    findViewById(R.id.onTheWay).setVisibility(View.VISIBLE);
                                                    //start route
                                                }
                                            });

                                            declineBut.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    db.collection("Rides").document(snapshot.getId()).update("status", "DECLINED");
                                                    requested = false;
                                                    googleMap.clear();
                                                    acceptBut.startAnimation(animFadeOut);
                                                    acceptBut.setVisibility(View.GONE);
                                                    declineBut.startAnimation(animFadeOut);
                                                    declineBut.setVisibility(View.GONE);
                                                }
                                            });




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

                    Date liveTime = new Date(System.currentTimeMillis() - 10000);
    if (newRide.getTimestamp() != null) {
        if (newRide.getTimestamp().after(liveTime)) {
            return true;
        } else {
            return false;
        }
    }
    return false;

}


    public void updateAndZoomLocation(GoogleMap mMap, FusedLocationProviderClient fusedLocation) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude())), 15));

            }
        });

    }

    @Override
    public void onBackPressed(){//open prompt are you sure?
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            locationManager.removeUpdates(locationListener);
            db.collection("Drivers").document(mAuth.getCurrentUser().getUid()).delete();
            Intent intent = new Intent(this, PreScreen.class);
            startActivity(intent);
        }

    }

}
