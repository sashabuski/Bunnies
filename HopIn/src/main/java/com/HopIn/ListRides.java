package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Extracts some info from all the available "Rides" documents on Firebase
 * The info is used to create a Ride object
 * List of Ride objects passed to RidesAdapter
 *
 */

public class ListRides extends AppCompatActivity {

    RecyclerView recview;
    List<Rides> datalist;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private RidesAdapter adapter;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        currentUser = new User();
        String email = mAuth.getCurrentUser().getEmail();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);

        recview=(RecyclerView)findViewById(R.id.recyclerView);
        recview.setLayoutManager(new LinearLayoutManager(this));
        datalist=new ArrayList<>();
        adapter=new RidesAdapter(datalist);
        recview.setAdapter(adapter);

        db=FirebaseFirestore.getInstance();
        db.collection("Rides").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list=queryDocumentSnapshots.getDocuments();

                        for(DocumentSnapshot d:list) {

                            Map<String, Object> testing1 = d.getData();
                            Timestamp date = (Timestamp) testing1.get("timestamp");
                            Date date1 = date.toDate();
                            String strDate = date1.toString();

                            //getting latitude and longitude of pick up location
                            // this will be passed later to getCompleteAddressString() function to find approx address
                            //only passing to function if we find documents of current user logged in.
                            HashMap geoPointRaw = (HashMap) testing1.get("pickupPoint");
                            Double latitude = (Double) geoPointRaw.get("latitude");
                            Double longitude = (Double) geoPointRaw.get("longitude");

                            //getting rider's email and name
                            HashMap riderpt1 = (HashMap) testing1.get("rider");
                            HashMap riderpt2 = (HashMap) riderpt1.get("user");
                            String riderEmail = (String) riderpt2.get("email");
                            //System.out.println("\nEmail of rider = " +riderEmail);
                            HashMap riderN1 = (HashMap) testing1.get("rider");
                            HashMap riderN2 = (HashMap) riderN1.get("user");
                            String riderFName = (String) riderN2.get("fName");
                            String riderLName = (String) riderN2.get("lName");
                            String riderName = riderFName +" "+ riderLName;

                            //getting driver's name and email
                            HashMap driverpt1 = (HashMap) testing1.get("driver");
                            HashMap driverpt2 = (HashMap) driverpt1.get("user");
                            String driverEmail = (String) driverpt2.get("email");
                            //System.out.println("\nEmail of driver = " +driverEmail);
                            HashMap driverN1 = (HashMap) testing1.get("rider");
                            HashMap driverN2 = (HashMap) driverN1.get("user");
                            String driverFName = (String) driverN2.get("fName");
                            String driverLName = (String) driverN2.get("lName");
                            String driverName = driverFName +" "+ driverLName;

                            //checking if the driver email matches the logged in user's email
                            if (email.equals(driverEmail)){
                                System.out.println("Logged user is Driver = "+driverEmail);
                                driverName="Yourself";
                                Rides obj = new Rides();
                                obj.setTimestamp(strDate);
                                obj.setDriverN(driverName);
                                obj.setDriverE(driverEmail);
                                obj.setRiderN(riderName);
                                obj.setRiderE(riderEmail);
                                String newAddr = getCompleteAddressString(latitude,longitude);
                                obj.setPickup(newAddr);
                                datalist.add(obj);
                            }
                            //checking if the rider's email from the document matches the logged in user's email
                            else if (email.equals(riderEmail)){
                                System.out.println("Logged user is Rider = "+riderEmail);
                                riderName="Yourself";
                                Rides obj = new Rides();
                                obj.setTimestamp(strDate);
                                obj.setDriverN(driverName);
                                obj.setDriverE(driverEmail);
                                obj.setRiderN(riderName);
                                obj.setRiderE(riderEmail);
                                String newAddr = getCompleteAddressString(latitude,longitude);
                                obj.setPickup(newAddr);
                                datalist.add(obj);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    //this function gets a list of addresses using the input latitude and longitude
    @SuppressLint("LongLogTag")
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAddress = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAddress = strReturnedAddress.toString();
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAddress;
    }
}