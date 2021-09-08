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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.HopIn.databinding.ActivityRiderMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    private FusedLocationProviderClient fusedLocation;
    private UserLocation currentUserLocation;
    private BitmapDescriptor icon;
    private ArrayList<CarClusterMarker> mClusterMarkers = new ArrayList<>();
    //private ConstraintLayout hopInCL;
    // private ConstraintLayout inCarCL;
    //private ConstraintLayout declinedCL;
    private View bottomSheetView;
    private BottomSheetBehavior bottomSheetBehavior;
    private Button mainButton;
    private TextView driverName;
    private ShapeableImageView driverPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocation = LocationServices.getFusedLocationProviderClient(RiderMapsActivity.this);
        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                GeoPoint geoPoint = new GeoPoint(task.getResult().getLatitude(), task.getResult().getLongitude());
                currentUserLocation.setGeoPoint(geoPoint);
                currentUserLocation.setTimestamp(null);
                db.collection("Riders").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);
            }
        });
        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        binding = ActivityRiderMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        currentUser = (User) (getIntent().getSerializableExtra("loggedUser"));
        currentUserLocation = new UserLocation(currentUser);

        bottomSheetView = (View) findViewById(R.id.bottomSheet);
        if (bottomSheetView == null) {
            System.out.println("LALALLALALALALLALLLALA");
        }
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

        mainButton = findViewById(R.id.mainButton);
        driverName = findViewById(R.id.driverName);
        driverPic = findViewById(R.id.profilePic);

        mainButton.setVisibility(View.GONE);
        driverName.setVisibility(View.GONE);
        driverPic.setVisibility(View.GONE);
        findViewById(R.id.loading).setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        findViewById(R.id.markerProfilePic).setVisibility(View.GONE);
        findViewById(R.id.driverName).setVisibility(View.GONE);
        findViewById(R.id.profilePic).setVisibility(View.GONE);
        findViewById(R.id.waitingText).setVisibility(View.GONE);

        findViewById(R.id.callBut).setVisibility(View.GONE);
        findViewById(R.id.chatBut).setVisibility(View.GONE);
        findViewById(R.id.transitAnimation).setVisibility(View.GONE);

        /*hopInCL = findViewById(R.id.hopInConstraintLayout);
        hopInCL.setVisibility(View.GONE);

        inCarCL = findViewById(R.id.inCarConstraintLayout);
        inCarCL.setVisibility(View.GONE);

        declinedCL = findViewById(R.id.declinedConstraintLayout);
        declinedCL.setVisibility(View.GONE);

        completeRide = findViewById(R.id.completeRideButton);
        completeRide.setVisibility(View.GONE); */
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        getDriversWithRealtimeUpdates(mMap, getCurrentFocus());
        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome "+currentUser.fName+", please select your ride.");
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
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                currentUserLocation.setGeoPoint(geoPoint);
                currentUserLocation.setTimestamp(null);
                db.collection("Riders").document(mAuth.getCurrentUser().getUid()).set(currentUserLocation);


            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method combines clusterManager/renderer/markers with realtime geoPoint updates from FireStore
     * DB Drivers collection to display current drivers as markers on the map in real time.
     *
     * @param googleMap
     * @param view
     */

    public void getDriversWithRealtimeUpdates(GoogleMap googleMap, View view) {


        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

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

                                if (isTimestampLive(user.getTimestamp())) {

                                    CarClusterMarker ccm = new CarClusterMarker(pls.latitude, pls.longitude, snapshot.toObject(UserLocation.class).getUser().fName, "jkjkjk", user);

                                    clusterManager.addItem(ccm);
                                    mClusterMarkers.add(ccm);
                                } else {

                                }
                            }
                            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<CarClusterMarker>() {
                                @Override
                                public boolean onClusterItemClick(CarClusterMarker item) {
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(item.getPosition()));
                                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                                    /*BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RiderMapsActivity.this, R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_request_sheet, (LinearLayout)findViewById(R.id.requestSheetContainer));
                                    TextView a = bottomSheetView.findViewById(R.id.name);
                                    a.setText(item.getUser().getUser().fName+" "+item.getUser().getUser().lName);
                                    bottomSheetDialog.setContentView(bottomSheetView);
*/
                                    findViewById(R.id.welcomeText).startAnimation(animFadeOut);
                                    findViewById(R.id.welcomeText).setVisibility(View.GONE);
                                    findViewById(R.id.carDriving).startAnimation(animFadeOut);
                                    findViewById(R.id.carDriving).setVisibility(View.GONE);

                                    driverName.setText(item.getUser().getUser().fName + " " + item.getUser().getUser().lName);
                                    mainButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Ride ride = new Ride(item.getUser(), currentUserLocation, null);

                                            db.collection("Rides").add(ride).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(RiderMapsActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                    String requestID = documentReference.getId();
                                                    listenForResponse(requestID);

                                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                                    mainButton.startAnimation(animFadeOut);
                                                    mainButton.setVisibility(View.GONE);
                                                    driverName.startAnimation(animFadeOut);
                                                    driverName.setVisibility(View.GONE);
                                                    findViewById(R.id.markerProfilePic).startAnimation(animFadeOut);
                                                    findViewById(R.id.markerProfilePic).setVisibility(View.GONE);
                                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                                    findViewById(R.id.loading).startAnimation(animFadeIn);
                                                    findViewById(R.id.loading).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.profilePic).startAnimation(animFadeIn);
                                                    findViewById(R.id.profilePic).setVisibility(View.VISIBLE);
                                                    TextView waitingText = (TextView) findViewById(R.id.waitingText);
                                                    waitingText.setText("Ride request sent to " + item.getUser().getUser().fName + ". Waiting for confirmation..");
                                                    waitingText.startAnimation(animFadeIn);
                                                    waitingText.setVisibility(View.VISIBLE);

                                                }
                                            });


                                        }
                                    });


                                    TextView name = findViewById(R.id.driverName);
                                    name.setText(item.getUser().getUser().fName + " " + item.getUser().getUser().lName);
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                                    name.startAnimation(animFadeIn);
                                    name.setVisibility(View.VISIBLE);
                                    findViewById(R.id.markerProfilePic).startAnimation(animFadeIn);
                                    findViewById(R.id.markerProfilePic).setVisibility(View.VISIBLE);
                                    mainButton.startAnimation(animFadeIn);
                                    mainButton.setVisibility(View.VISIBLE);

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

    public void listenForResponse(String reqID) {
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
                                    /*BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RiderMapsActivity.this, R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_request_sheet, (LinearLayout)findViewById(R.id.requestSheetContainer));
                                    TextView a = bottomSheetView.findViewById(R.id.name);
                                    a.setText("RIDE ACCEPTED");
                                    bottomSheetDialog.setContentView(bottomSheetView);

                                    button.setText("In car");
                                    bottomSheetDialog.setCancelable(false);
                                   */
                                //TextView a = findViewById(R.id.name2);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                                findViewById(R.id.loading).setVisibility(View.GONE);
                                findViewById(R.id.profilePic).startAnimation(animFadeOut);
                                findViewById(R.id.profilePic).setVisibility(View.GONE);
                                findViewById(R.id.waitingText).setVisibility(View.GONE);
                                driverName.setText(newRide.getDriver().getUser().fName + " " + newRide.getDriver().getUser().lName + " has accepted your request!");
                                driverName.startAnimation(animFadeIn);
                                driverName.setVisibility(View.VISIBLE);
                                mainButton.setText("Confirm Pickup");
                                mainButton.startAnimation(animFadeIn);
                                mainButton.setVisibility(View.VISIBLE);
                                findViewById(R.id.markerProfilePic).startAnimation(animFadeIn);
                                findViewById(R.id.markerProfilePic).setVisibility(View.VISIBLE);
                                findViewById(R.id.callBut).setVisibility(View.VISIBLE);
                                findViewById(R.id.chatBut).setVisibility(View.VISIBLE);
                                // Button button = findViewById(R.id.inCarButton);




                                mainButton.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                        db.collection("Rides").document(reqID).update("status", "TRANSIT");
                                        findViewById(R.id.markerProfilePic).startAnimation(animFadeOut);
                                        findViewById(R.id.markerProfilePic).setVisibility(View.GONE);

                                        driverName.startAnimation(animFadeOut);
                                        driverName.setVisibility(View.GONE);
                                        mainButton.startAnimation(animFadeOut);
                                        mainButton.setVisibility(View.GONE);
                                        TextView transitText = findViewById(R.id.transitText);
                                        transitText.setText("In transit..");
                                        transitText.startAnimation(animFadeIn);
                                        transitText.setVisibility(View.VISIBLE);
                                        findViewById(R.id.transitAnimation).startAnimation(animFadeIn);
                                        findViewById(R.id.transitAnimation).setVisibility(View.VISIBLE);
                                        mainButton.setText("Complete Ride");
                                        findViewById(R.id.callBut).setVisibility(View.GONE);
                                        findViewById(R.id.chatBut).setVisibility(View.GONE);
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        findViewById(R.id.markerProfilePic).startAnimation(animFadeIn);
                                        mainButton.startAnimation(animFadeIn);
                                        mainButton.setVisibility(View.VISIBLE);
                                        mainButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                findViewById(R.id.transitAnimation).startAnimation(animFadeOut);
                                                findViewById(R.id.transitAnimation).setVisibility(View.GONE);
                                                transitText.startAnimation(animFadeOut);
                                                transitText.setVisibility(View.GONE);
                                                mainButton.startAnimation(animFadeOut);
                                                mainButton.setVisibility(View.GONE);
                                            }
                                        });
                                    }

                                });

                                mainButton.setClickable(false);
                                mainButton.setAlpha(.5f);

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

                                                                                                                     if (newRide.getStatus().equals("ARRIVED")) {

                                                                                                                                 mainButton.setAlpha(1f);
                                                                                                                                 mainButton.setClickable(true);
                                                                                                                             }
                                                                                                                         }
                                                                                                                     }
                                                                                                                 }
                                                                                                             }
                                                                                                         });




                                // inCarCL.setVisibility(View.VISIBLE);

                                //open
                            } else if (newRide.getStatus().equals("DECLINED")) {
                                //OPEN
                                db.collection("Rides").document(reqID).update("status", "TERMINATED");

                                   /* BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RiderMapsActivity.this, R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_request_sheet, (LinearLayout)findViewById(R.id.requestSheetContainer));
                                    TextView a = bottomSheetView.findViewById(R.id.name);
                                    a.setText("RIDE DECLINED");
                                    bottomSheetDialog.setContentView(bottomSheetView);*/


                                //  Button button = findViewById(R.id.okButton);


                                // button.setOnClickListener(new View.OnClickListener() {

                                // @Override
                                // public void onClick(View view) {

                                //       declinedCL.setVisibility(View.GONE);

                            }
                            //  });
                            //  declinedCL.setVisibility(View.VISIBLE);
                        }
                    }
                }

                // }
            }
            // });

        });
    }

    public boolean isTimestampLive(Date date) {

        Date liveTime = new Date(System.currentTimeMillis() - 5000);
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
    public void onBackPressed() {//open prompt are you sure?
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            locationManager.removeUpdates(locationListener);
            db.collection("Riders").document(mAuth.getCurrentUser().getUid()).delete();
            Intent intent = new Intent(this, PreScreen.class);
            startActivity(intent);
        }


    }
}




