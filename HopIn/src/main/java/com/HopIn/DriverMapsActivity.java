package com.HopIn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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
 * @Author: Sasha Buskin
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
    private Button acceptBut;
    private View bottomSheetView, dashboardSheetView;
    private BottomSheetBehavior bottomSheetBehavior,dashboardSheetBehavior;
    private Animation animFadeIn;
    private Animation animFadeOut;
    private TextView requestText, welcomeText, welcomeTip, dashboardUserName, ArrivedText;
    private LottieAnimationView carDriving;
    private String requestID;
    private Button passager_info_tip;
   
  
  /**
     * onCreate sets up bottomsheet behaviors for dashboard, bottomsheet.
     * Sets up onClickListeners for chat button and menu button.
     * Uses fusedLocationClient to get initial location of user.
     *
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);

        bottomSheetView = (View)findViewById(R.id.driverBottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

        dashboardSheetView = (View)findViewById(R.id.dashboard);
        dashboardSheetBehavior = BottomSheetBehavior.from(dashboardSheetView);

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

         dashboardUserName =  findViewById(R.id.dashboardUserName);
         dashboardUserName.setText(currentUser.fName+" "+currentUser.lName);
         findViewById(R.id.chatBut).setVisibility(View.GONE);
         welcomeTip = findViewById(R.id.welcomeTip);
         welcomeText = findViewById(R.id.welcomeText);
         declineBut = findViewById(R.id.declineButton);
         acceptBut = findViewById(R.id.acceptButton);
         declineBut.setVisibility(View.GONE);
         acceptBut.setVisibility(View.GONE);
         requestText = findViewById(R.id.requestText);
         carDriving = findViewById(R.id.carDriving);
         requestText.setVisibility(View.GONE);
         ArrivedText = findViewById(R.id.ArrivedText);
         ArrivedText.setVisibility(View.GONE);
         findViewById(R.id.onTheWay).setVisibility(View.GONE);
         findViewById(R.id.arrivedButton).setVisibility(View.GONE);
         findViewById(R.id.arriveTip).setVisibility(View.GONE);
         findViewById(R.id.requestPic).setVisibility(View.GONE);
         dashboardSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
         FloatingActionButton menuButton = findViewById(R.id.menuButton);
         findViewById(R.id.confirmPickupButton).setVisibility(View.GONE);


        findViewById(R.id.chatBut).setOnClickListener(new ChatButtonClickListener());


        menuButton.setOnClickListener(new DashboardClickListener());
/*    
        Jianyi passager button
        
        passager_info_tip = findViewById(R.id.passager_info_tip);
        passager_info_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivity_passager();
            }
        });
    }

    private void openactivity_passager(){
        

        Intent intent = new Intent(DriverMapsActivity.this,passager.class);
        intent.putExtra("pname","test");
        intent.putExtra("pupp","st");
        intent.putExtra("destination","se");
        intent.putExtra("pt","33");
        startActivity(intent);
*/
    }

    /**
     * Implements a locationListener that sends real time geopoint updates to the driver DB collection
     *
     * @Author: Sasha Buskin
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        listenForRequests(mMap );

        welcomeText.setText("Good Morning "+currentUser.fName+".");

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    /**
     * Handles incoming requests. Detects user specific ride requests through snapshotListener, handles display change,
     * handles response i.e. accept/decline db updates.
     *
     * @Author: Sasha Buskin
     * @param googleMap
     */

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

                                            requestID = snapshot.getId();
                                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                            requested = true;
                                            PickupPt pickupPt = newRide.getPickupPoint();
                                            LatLng pickUp = new LatLng(pickupPt.getLatitude(), pickupPt.getLongitude());
                                            googleMap.addMarker(new MarkerOptions().position(pickUp).title(newRide.getRider().getUser().fName));
                                            CameraUpdate center = CameraUpdateFactory.newLatLng(pickUp);
                                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                                            googleMap.moveCamera(center);
                                            googleMap.animateCamera(zoom);

                                            hideWelcomeDisplay();

                                            showRequestDisplay(newRide);

                                            acceptBut.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                db.collection("Rides").document(snapshot.getId()).update("status", "ACCEPTED");

                                                    LatLng a = new LatLng(currentUserLocation.getGeoPoint().getLatitude(),currentUserLocation.getGeoPoint().getLongitude());
                                                    findViewById(R.id.chatBut).setVisibility(View.VISIBLE);
                                                    hideRequestDisplay();

                                                    findViewById(R.id.arrivedButton).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            db.collection("Rides").document(snapshot.getId()).update("status", "ARRIVED");
                                                            googleMap.clear();
                                                            arrivedButtonDisplayChange(newRide);

                                                            findViewById(R.id.confirmPickupButton).setOnClickListener(new ConfirmPickupClickListener());

                                                        }
                                                    });

                                                   showOnRouteDisplay(newRide);

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

    class DashboardClickListener implements View.OnClickListener {
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
    }

    class ChatButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent;
            intent = new Intent(DriverMapsActivity.this, ChatActivity.class);

            intent.putExtra("ReqID", requestID);
            intent.putExtra("userType", "Driver");

            startActivity(intent);
        }
    }

    class ConfirmPickupClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            ArrivedText.startAnimation(animFadeOut);
            ArrivedText.setVisibility(View.GONE);
            findViewById(R.id.confirmPickupButton).startAnimation(animFadeOut);
            findViewById(R.id.confirmPickupButton).setVisibility(View.GONE);
            findViewById(R.id.chatBut).startAnimation(animFadeOut);
            findViewById(R.id.chatBut).setVisibility(View.GONE);

            welcomeText.startAnimation(animFadeIn);
            welcomeText.setVisibility(View.VISIBLE);
            welcomeTip.startAnimation(animFadeIn);
            welcomeTip.setVisibility(View.VISIBLE);
            carDriving.startAnimation(animFadeIn);
            carDriving.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }


    /**
     * Methods to change what is displayed on the bottom sheet UI.
     *
     * @Author: Sasha Buskin
     * @param newRide
     */

   public void showOnRouteDisplay(Ride newRide){

       findViewById(R.id.arriveTip).startAnimation(animFadeIn);
       findViewById(R.id.arriveTip).setVisibility(View.VISIBLE);
       findViewById(R.id.arrivedButton).startAnimation(animFadeIn);
       findViewById(R.id.arrivedButton).setVisibility(View.VISIBLE);
       requestText.setText("On route to "+newRide.getRider().getUser().fName+"!");
       requestText.startAnimation(animFadeIn);
       requestText.setVisibility(View.VISIBLE);
       findViewById(R.id.onTheWay).startAnimation(animFadeIn);
       findViewById(R.id.onTheWay).setVisibility(View.VISIBLE);
   }

    public void arrivedButtonDisplayChange(Ride newRide){
       ArrivedText.setText("Arrived to pick up "+newRide.getRider().getUser().fName);
       ArrivedText.startAnimation(animFadeIn);
       ArrivedText.setVisibility(View.VISIBLE);
       findViewById(R.id.confirmPickupButton).startAnimation(animFadeIn);
       findViewById(R.id.confirmPickupButton).setVisibility(View.VISIBLE);
       findViewById(R.id.arriveTip).startAnimation(animFadeOut);
       findViewById(R.id.arriveTip).setVisibility(View.GONE);
       findViewById(R.id.arrivedButton).startAnimation(animFadeOut);
       findViewById(R.id.arrivedButton).setVisibility(View.GONE);
       requestText.startAnimation(animFadeOut);
       requestText.setVisibility(View.GONE);
       findViewById(R.id.onTheWay).startAnimation(animFadeOut);
       findViewById(R.id.onTheWay).setVisibility(View.GONE);
   }

   public void hideWelcomeDisplay(){

       welcomeTip.startAnimation(animFadeOut);
       welcomeTip.setVisibility(View.GONE);
       welcomeText.startAnimation(animFadeOut);
       welcomeText.setVisibility(View.GONE);
       carDriving.startAnimation(animFadeOut);
       carDriving.setVisibility(View.GONE);

   }

    public void showRequestDisplay(Ride newRide){

       findViewById(R.id.requestPic).startAnimation(animFadeIn);
       findViewById(R.id.requestPic).setVisibility(View.VISIBLE);
       acceptBut.startAnimation(animFadeIn);
       acceptBut.setVisibility(View.VISIBLE);
       declineBut.startAnimation(animFadeIn);
       declineBut.setVisibility(View.VISIBLE);
       requestText.setText("New ride request from "+newRide.getRider().getUser().fName+"!");
       requestText.startAnimation(animFadeIn);
       requestText.setVisibility(View.VISIBLE);

   }

    public void hideRequestDisplay() {

        findViewById(R.id.requestPic).startAnimation(animFadeOut);
        findViewById(R.id.requestPic).setVisibility(View.GONE);
        requestText.startAnimation(animFadeOut);
        requestText.setVisibility(View.GONE);
        acceptBut.startAnimation(animFadeOut);
        acceptBut.setVisibility(View.GONE);
        declineBut.startAnimation(animFadeOut);
        declineBut.setVisibility(View.GONE);
    }


    /**
     * Method that double checks whether a ride request is new i.e. within the last 10 seconds.
     *
     * @Author: Sasha Buskin
     * @param newRide
     * @return
     */

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
        }
        else if(dashboardSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {


            bottomSheetBehavior.setPeekHeight(80);

            dashboardSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
       }
        else {

           locationManager.removeUpdates(locationListener);
           db.collection("Drivers").document(mAuth.getCurrentUser().getUid()).delete();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure you want to leave? All rides will be cancelled");
            alertDialogBuilder.setPositiveButton("yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        //if user click yes, the user will get redirect back to prescreen class (choose between driver or rider class)
                        public void onClick(DialogInterface popupmsg, int yestxtmsg) {
                            Intent intent = new Intent(DriverMapsActivity.this, PreScreen.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                            Toast.makeText(DriverMapsActivity.this, "Leaving Map", Toast.LENGTH_LONG).show();
                        }
                    });

            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                //if user click no, 'staying in map' text will pop up, and user will stay in map class
                public void onClick(DialogInterface popupmsg, int notxtmsg) {
                    Toast.makeText(DriverMapsActivity.this, "Staying in Map", Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
           
       }


    }


}
