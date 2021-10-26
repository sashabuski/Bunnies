package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                            HashMap riderpt1 = (HashMap) testing1.get("rider");
                            HashMap riderpt2 = (HashMap) riderpt1.get("user");
                            String riderEmail = (String) riderpt2.get("email");
                            //System.out.println("\nEmail of rider = " +riderEmail);
                            HashMap riderN1 = (HashMap) testing1.get("rider");
                            HashMap riderN2 = (HashMap) riderN1.get("user");
                            String riderFName = (String) riderN2.get("fName");
                            String riderLName = (String) riderN2.get("lName");
                            String riderName = riderFName +" "+ riderLName;

                            HashMap driverpt1 = (HashMap) testing1.get("driver");
                            HashMap driverpt2 = (HashMap) driverpt1.get("user");
                            String driverEmail = (String) driverpt2.get("email");
                            //System.out.println("\nEmail of driver = " +driverEmail);
                            HashMap driverN1 = (HashMap) testing1.get("rider");
                            HashMap driverN2 = (HashMap) driverN1.get("user");
                            String driverFName = (String) driverN2.get("fName");
                            String driverLName = (String) driverN2.get("lName");
                            String driverName = driverFName +" "+ driverLName;

                            if (email.equals(driverEmail)){
                                System.out.println("Logged user is Driver = "+driverEmail);
                                driverName="Yourself";
                                Rides obj = new Rides();
                                obj.setTimestamp(strDate);
                                obj.setDriverN(driverName);
                                obj.setDriverE(driverEmail);
                                obj.setRiderN(riderName);
                                obj.setRiderE(riderEmail);
                                datalist.add(obj);
                            }
                            else if (email.equals(riderEmail)){
                                System.out.println("Logged user is Rider = "+riderEmail);
                                riderName="Yourself";
                                Rides obj = new Rides();
                                obj.setTimestamp(strDate);
                                obj.setDriverN(driverName);
                                obj.setDriverE(driverEmail);
                                obj.setRiderN(riderName);
                                obj.setRiderE(riderEmail);
                                datalist.add(obj);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}