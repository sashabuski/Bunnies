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
import com.HopIn.databinding.ActivityRiderMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Rider Map activity uses documentSnapshots in onEvent listener to detect and read changes
 * in the Drivers FireStore collection in real time. This is used to create and continuously update
 * clusterMarkers that represent and contain objects corresponding with each driver.
 *
 * This activity also sends the riders goePoint to the Riders Firestore collection in real time.
 *
 * ---Need to fix when location updates are turned off + remove document from "drivers" DB collection.
 * ---(onStop, onResume, onDestroy)
 */
public class RiderMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRiderMapsBinding binding;
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
                currentUserLocation.setTimestamp(null);
                db.collection("Riders").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);


                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            }
        };

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * This method combines clusterManager/renderer/markers with realtime geoPoint updates from FireStore
     * DB Drivers collection to display current drivers as markers on the map in real time.
     * @param googleMap
     * @param view
     */

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
                                UserLocation user = snapshot.toObject(UserLocation.class);

                                if(isTimestampLive(user.getTimestamp())) {

                                    CarClusterMarker ccm = new CarClusterMarker(pls.latitude, pls.longitude, snapshot.toObject(UserLocation.class).getUser().fName, "jkjkjk", user);
                                    clusterManager.addItem(ccm);
                                    mClusterMarkers.add(ccm);
                                }else{

                                }
                            }
                            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<CarClusterMarker>() {
                                @Override
                                public boolean onClusterItemClick(CarClusterMarker item) {

                                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RiderMapsActivity.this, R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_request_sheet, (LinearLayout)findViewById(R.id.requestSheetContainer));
                                    TextView a = bottomSheetView.findViewById(R.id.name);
                                    a.setText(item.getUser().getUser().fName+" "+item.getUser().getUser().lName);
                                    bottomSheetDialog.setContentView(bottomSheetView);
                                    Button button = (Button)bottomSheetDialog.findViewById(R.id.requestButton);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        Ride ride = new Ride(item.getUser(),currentUserLocation, null);

                                        db.collection("Rides").add(ride).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                String requestID = documentReference.getId();
                                                listenForResponse(requestID);
                                            }
                                        });
                                        bottomSheetDialog.hide();

                                        }
                                    });

                                    bottomSheetDialog.show();
                                    return true;
                                }
                            });
                            clusterManager.cluster();

                        } else {
                            Log.e("Snapshot Error", "onEvent: query snapshot was null");
                        }
                    }
                });
    }

    public void listenForResponse(String reqID){
        FirebaseFirestore.getInstance()
                .collection("Rides").document(reqID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error != null) {

                    return;
                }
                if (value != null) {

                        Ride newRide = value.toObject(Ride.class);

                        if (newRide.getDriver().getUser().email != null && currentUser.email != null) {

                            if (newRide.getRider().getUser().email.equals(currentUser.email)) {

                                if (newRide.getStatus().equals("ACCEPTED")) {
                                    db.collection("Rides").document(reqID).update("status", "PICKUP");
                                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RiderMapsActivity.this, R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_request_sheet, (LinearLayout)findViewById(R.id.requestSheetContainer));
                                    TextView a = bottomSheetView.findViewById(R.id.name);
                                    a.setText("RIDE ACCEPTED");
                                    bottomSheetDialog.setContentView(bottomSheetView);
                                    Button button = (Button)bottomSheetDialog.findViewById(R.id.requestButton);
                                    button.setText("In car");
                                    bottomSheetDialog.setCancelable(false);
                                    button.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            db.collection("Rides").document(reqID).update("status", "TRANSIT");

                                            bottomSheetDialog.hide();

                                        }
                                    });

                                    bottomSheetDialog.show();

                                    //open
                                } else if (newRide.getStatus().equals("DECLINED")) {
                                //OPEN
                                    db.collection("Rides").document(reqID).update("status", "TERMINATED");

                                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RiderMapsActivity.this, R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_request_sheet, (LinearLayout)findViewById(R.id.requestSheetContainer));
                                    TextView a = bottomSheetView.findViewById(R.id.name);
                                    a.setText("RIDE DECLINED");
                                    bottomSheetDialog.setContentView(bottomSheetView);
                                    Button button = (Button)bottomSheetDialog.findViewById(R.id.requestButton);
                                    button.setText("OK");

                                    button.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            bottomSheetDialog.hide();

                                        }
                                    });
                                    bottomSheetDialog.show();
                                }
                            }
                        }

                }
            }
        });

    }

  public boolean isTimestampLive(Date date){

      Date liveTime = new Date(System.currentTimeMillis()- 5000);
      if (date != null) {
          if (date.after(liveTime)) {

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
        db.collection("Riders").document(mAuth.getCurrentUser().getUid()).delete();
        Intent intent = new Intent(this, PreScreen.class);
        startActivity(intent);

    }
}

