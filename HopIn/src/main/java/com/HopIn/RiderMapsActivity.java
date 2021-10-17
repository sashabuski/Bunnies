package com.HopIn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private View bottomSheetView, dashboardSheetView;
    private BottomSheetBehavior bottomSheetBehavior,dashboardSheetBehavior;
    private Button mainButton, selectPointButton;
    private TextView driverName, carModelText, carPlateText, codeText;
    private ShapeableImageView driverPic, markerProfilePic;
    private Animation animFadeIn, animFadeOut;
    private TextView transitText, welcomeText, name, dashboardUserName, pointSelectedText;
    private LottieAnimationView carDriving, loading;
    private LatLng pickupPoint;
    private ExtendedFloatingActionButton confirmPickupPointButton;
    private Boolean pointSelected = false;
    private String requestID;
    private RiderSystemStatus systemStatus = RiderSystemStatus.RESTING_MAP;
    private FloatingActionButton menuButton;

    public enum RiderSystemStatus {
        RESTING_MAP,
        SELECTING_POINT,
        POINT_SELECTED,
        POINT_CONFIRMED,
        DRIVER_SELECTED,
        DRIVER_REQUESTED,
        AWAITING_DRIVER,
        DRIVER_DECLINED,
        DRIVER_ARRIVED,
        IN_TRANSIT;

    }

    /**
     * OnCreate sets up bottomsheet behaviors for dashboard, bottomsheet.
     * Sets up onClickListeners for chat button and menu button.
     * Uses fusedLocationClient to get initial location of user.
     *
     * @param savedInstanceState
     */
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

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        binding = ActivityRiderMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        currentUser = (User) (getIntent().getSerializableExtra("loggedUser"));
        currentUserLocation = new UserLocation(currentUser);

        bottomSheetView = (View)findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        dashboardSheetView = (View)findViewById(R.id.dashboard);
        dashboardSheetBehavior = BottomSheetBehavior.from(dashboardSheetView);

        dashboardUserName =  findViewById(R.id.dashboardUserName);
        dashboardUserName.setText(currentUser.fName+" "+currentUser.lName);
        dashboardSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        pointSelectedText = findViewById(R.id.pointSelectedText);
        confirmPickupPointButton = findViewById(R.id.confirmPickupPointButton);

        selectPointButton = findViewById(R.id.selectPointButton);
        mainButton = findViewById(R.id.mainButton);
        driverName = findViewById(R.id.driverName);
        driverPic = findViewById(R.id.profilePic);
        name = findViewById(R.id.driverName);
        transitText = findViewById(R.id.transitText);
        welcomeText = findViewById(R.id.welcomeText);
        carDriving = findViewById(R.id.carDriving);
        loading =  findViewById(R.id.loading);
        markerProfilePic = findViewById(R.id.markerProfilePic);
        carModelText = findViewById(R.id.carModel);
        carPlateText = findViewById(R.id.carNumberPlate);
        codeText = findViewById(R.id.verificationCode);
        menuButton = findViewById(R.id.menuButton);

        carModelText.setVisibility(View.GONE);
        carPlateText.setVisibility(View.GONE);
        codeText.setVisibility(View.GONE);
        confirmPickupPointButton.setVisibility(View.GONE);
        selectPointButton.setVisibility(View.GONE);
        mainButton.setVisibility(View.GONE);
        driverName.setVisibility(View.GONE);
        driverPic.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        markerProfilePic.setVisibility(View.GONE);
        findViewById(R.id.waitingText).setVisibility(View.GONE);
        findViewById(R.id.chatBut).setVisibility(View.GONE);
        findViewById(R.id.transitAnimation).setVisibility(View.GONE);

        findViewById(R.id.chatBut).setClickable(true);
        findViewById(R.id.chatBut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;
                intent = new Intent(RiderMapsActivity.this, ChatActivity.class);

                intent.putExtra("ReqID", requestID);
                intent.putExtra("userType", "Rider");

                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dashboardSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                {
                    bottomSheetBehavior.setPeekHeight(80);
                    dashboardSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                }else {

                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    dashboardSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    dashboardSheetBehavior.setDraggable(false);
                }
            }
        });
    }

    /**
     * onMapReady sets up selectPointButton and confirmPointButton onClickListener.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
        welcomeText.setText("Welcome "+currentUser.fName);
        selectPointButton.setText("Select pickup point");
        selectPointButton.setVisibility(View.VISIBLE);
        systemStatus = RiderSystemStatus.RESTING_MAP;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        getDriversWithRealtimeUpdates(mMap, getCurrentFocus());

        selectPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                confirmPickupPointButton.setAlpha(.7f);
                confirmPickupPointButton.setClickable(false);
                confirmPickupPointButton.startAnimation(animFadeIn);
                confirmPickupPointButton.setVisibility(View.VISIBLE);

                systemStatus = RiderSystemStatus.SELECTING_POINT;

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {

                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(point));
                        systemStatus = RiderSystemStatus.POINT_SELECTED;
                        pickupPoint = point;
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                        confirmPickupPointButton.setAlpha(1f);
                        confirmPickupPointButton.setClickable(true);
                        confirmPickupPointButton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                systemStatus = RiderSystemStatus.POINT_CONFIRMED;
                                welcomeText.setVisibility(View.GONE);
                                selectPointButton.setVisibility(View.GONE);
                                confirmPickupPointButton.startAnimation(animFadeOut);
                                bottomSheetBehavior.setPeekHeight(80);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                confirmPickupPointButton.setVisibility(View.GONE);
                                mMap.setOnMapClickListener(null);
                                pointSelected = true;
                                pointSelectedText.setVisibility(View.VISIBLE);
                                pointSelectedText.setText("Pickup point selected, please select your ride.");
                            }
                        });
                    }
                });
            }
        });

     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);

        updateAndZoomLocation(mMap, fusedLocation);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

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
     * Handles onClusterItemClick and sending a request to corresponding driver.
     *
     *
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


                                    systemStatus = RiderSystemStatus.DRIVER_SELECTED;

                                    if (pointSelected == false) {
                                        Toast.makeText(RiderMapsActivity.this, "Please select your pickup point.", Toast.LENGTH_SHORT).show();


                                    } else {
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(item.getPosition()));
                                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                                        driverName.setText(item.getUser().getUser().fName + " " + item.getUser().getUser().lName);
                                        carPlateText.setText(item.getUser().getUser().carNumber);
                                        driverName.setText(item.getUser().getUser().fName + " " + item.getUser().getUser().lName);

                                        hideWelcomeShowDriverDisplay(item);

                                        mainButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                systemStatus = RiderSystemStatus.DRIVER_REQUESTED;

                                                pointSelected = false;
                                                PickupPt pickupPt = new PickupPt(pickupPoint.latitude, pickupPoint.longitude);
                                                Ride ride = new Ride(item.getUser(), currentUserLocation, null, pickupPt);

                                                db.collection("Rides").add(ride).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {

                                                        Toast.makeText(RiderMapsActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();

                                                        requestID = documentReference.getId();
                                                        listenForResponse(requestID);
                                                        showWaitingForResponseDisplay(item);
                                                    }
                                                });
                                            }
                                        });

                                        return true;
                                    }
                                    return false;
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

    /**
     * Method to listen for response after ride has been requested..
     *
     * @param reqID
     */
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

                                systemStatus = RiderSystemStatus.AWAITING_DRIVER;

                                showConfirmPickupDisplay(newRide);

                                mainButton.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        db.collection("Rides").document(reqID).update("status", "TRANSIT");

                                        showTransitDisplay();

                                        mainButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                systemStatus = RiderSystemStatus.IN_TRANSIT;

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

                                listenForArrival(reqID);
                            }
                            else if (newRide.getStatus().equals("DECLINED")) {

                                db.collection("Rides").document(reqID).update("status", "TERMINATED");

                                systemStatus = RiderSystemStatus.DRIVER_DECLINED;

                                showDeclinedDisplay(newRide);

                                mainButton.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        
                                        systemStatus = RiderSystemStatus.RESTING_MAP;
                                        backToHomeDisplay();

                                    }
                                });
                            }
                        }
                    }
                }
            }
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


    /**
     * Method to listen for when the ride object's status has been changed to ARRIVED in the database, and notifies rider when driver has arrived.
     *
     *
     * @param reqID
     */
    public void listenForArrival(String reqID){

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

                                systemStatus = RiderSystemStatus.DRIVER_ARRIVED;

                                driverName.startAnimation(animFadeOut);
                                driverName.setText("Your driver has arrived!");
                                Toast.makeText(RiderMapsActivity.this, "Driver has arrived!", Toast.LENGTH_LONG).show();
                                mainButton.setAlpha(1f);
                                mainButton.setClickable(true);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Methods to change what is displayed on the bottom sheet UI.
     *
     * @Author: Sasha Buskin
     *
     */
    public void backToHomeDisplay() {

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        driverName.setVisibility(View.GONE);
        mainButton.setVisibility(View.GONE);
        welcomeText.startAnimation(animFadeIn);
        welcomeText.setVisibility(View.VISIBLE);
        carDriving.startAnimation(animFadeIn);
        carDriving.setVisibility(View.VISIBLE);
        carPlateText.setVisibility(View.GONE);
    }

    public void showDeclinedDisplay(Ride newRide){

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        findViewById(R.id.loading).setVisibility(View.GONE);
        findViewById(R.id.profilePic).startAnimation(animFadeOut);
        findViewById(R.id.profilePic).setVisibility(View.GONE);
        findViewById(R.id.waitingText).setVisibility(View.GONE);
        driverName.setText("Sorry, "+newRide.getDriver().getUser().fName + " " + newRide.getDriver().getUser().lName + " is too busy to pick you up..");
        driverName.startAnimation(animFadeIn);
        driverName.setVisibility(View.VISIBLE);
        mainButton.setText("Select New Ride");
        mainButton.startAnimation(animFadeIn);
        mainButton.setVisibility(View.VISIBLE);
        carPlateText.setVisibility(View.VISIBLE);

    }

    public void hideWelcomeShowDriverDisplay(CarClusterMarker item) { welcomeText.startAnimation(animFadeOut);

        pointSelectedText.setVisibility(View.GONE);
        welcomeText.setVisibility(View.GONE);
        carDriving.startAnimation(animFadeOut);
        carDriving.setVisibility(View.GONE);
        name.setText(item.getUser().getUser().fName + " " + item.getUser().getUser().lName);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        name.startAnimation(animFadeIn);
        name.setVisibility(View.VISIBLE);
        markerProfilePic.startAnimation(animFadeIn);
        markerProfilePic.setVisibility(View.VISIBLE);
        mainButton.setText("Request ride.");
        mainButton.startAnimation(animFadeIn);
        mainButton.setVisibility(View.VISIBLE);
    }

    public void showTransitDisplay(){

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        markerProfilePic.startAnimation(animFadeOut);
        markerProfilePic.setVisibility(View.GONE);
        driverName.startAnimation(animFadeOut);
        driverName.setVisibility(View.GONE);
        mainButton.startAnimation(animFadeOut);
        mainButton.setVisibility(View.GONE);
        transitText.setText("In transit..");
        transitText.startAnimation(animFadeIn);
        transitText.setVisibility(View.VISIBLE);
        findViewById(R.id.transitAnimation).startAnimation(animFadeIn);
        findViewById(R.id.transitAnimation).setVisibility(View.VISIBLE);
        mainButton.setText("Complete Ride");
        findViewById(R.id.chatBut).setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        findViewById(R.id.markerProfilePic).startAnimation(animFadeIn);
        mainButton.startAnimation(animFadeIn);
        mainButton.setVisibility(View.VISIBLE);
        carPlateText.setVisibility(View.GONE);
    }

    public void showConfirmPickupDisplay(Ride newRide){

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
        findViewById(R.id.chatBut).setVisibility(View.VISIBLE);
        mainButton.setClickable(false);
        mainButton.setAlpha(.5f);
        carPlateText.setText(newRide.getDriver().getUser().carNumber);
        carPlateText.setVisibility(View.VISIBLE);
    }

    public void showWaitingForResponseDisplay(CarClusterMarker item){

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
        carPlateText.setVisibility(View.GONE);
    }

    /**
     *
     * onBackPressed modified for varying functions dependant upon the current SYSTEM STATUS ENUM
     *
     * @Author: Sasha Buskin
     */


    @Override
    public void onBackPressed() {//open prompt are you sure?

        if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else if(systemStatus == RiderSystemStatus.SELECTING_POINT){

            mMap.setOnMapClickListener(null);
            bottomSheetBehavior.setPeekHeight(80);
            confirmPickupPointButton.setVisibility(View.GONE);
            pointSelectedText.setVisibility(View.GONE);
            selectPointButton.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.VISIBLE);
            systemStatus = RiderSystemStatus.RESTING_MAP;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else if(systemStatus == RiderSystemStatus.POINT_SELECTED){

            mMap.clear();
            pickupPoint = null;
            confirmPickupPointButton.setVisibility(View.VISIBLE);
            confirmPickupPointButton.setClickable(false);
            confirmPickupPointButton.setAlpha(.7f);
            systemStatus = RiderSystemStatus.SELECTING_POINT;

        }
        else if(systemStatus == RiderSystemStatus.POINT_CONFIRMED){

            mMap.clear();
            mMap.setOnMapClickListener(null);
            bottomSheetBehavior.setPeekHeight(80);
            confirmPickupPointButton.setVisibility(View.GONE);
            pointSelectedText.setVisibility(View.GONE);
            selectPointButton.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.VISIBLE);
            systemStatus = RiderSystemStatus.RESTING_MAP;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) && systemStatus == RiderSystemStatus.DRIVER_SELECTED){

            mMap.clear();
            mMap.setOnMapClickListener(null);
            bottomSheetBehavior.setPeekHeight(80);
            markerProfilePic.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
            pointSelectedText.setVisibility(View.GONE);
            mainButton.setVisibility(View.GONE);
            carDriving.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.VISIBLE);
            selectPointButton.setVisibility(View.VISIBLE);
            systemStatus = RiderSystemStatus.RESTING_MAP;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        }
        else if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) && (dashboardSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) && (systemStatus == RiderSystemStatus.RESTING_MAP || systemStatus == RiderSystemStatus.DRIVER_REQUESTED
                || systemStatus == RiderSystemStatus.AWAITING_DRIVER|| systemStatus == RiderSystemStatus.IN_TRANSIT || systemStatus == RiderSystemStatus.DRIVER_ARRIVED)){

            //are you sure you wanna leave bruh all rides will be cancelled.

            locationManager.removeUpdates(locationListener);
            db.collection("Riders").document(mAuth.getCurrentUser().getUid()).delete();
            Intent intent = new Intent(this, PreScreen.class);
            startActivity(intent);
        }
        else if(dashboardSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            bottomSheetBehavior.setPeekHeight(80);
            dashboardSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
}




