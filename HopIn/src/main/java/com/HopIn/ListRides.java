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

    //private RecyclerView recyclerView;
    RecyclerView recview;

    //private List<Rides> ridesList;
    //ArrayList<model> datalist;
    List<Rides> datalist;

    //FirebaseFirestore db;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();

    //myadapter adapter;
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
                            //for (Map.Entry<String, Object> entry : testing1.entrySet()) {
                            //    System.out.println("\n" + entry.getKey() + " : " + entry.getValue()+"\n");
                            //}
                            Timestamp date = (Timestamp) testing1.get("timestamp");
                            Date date1 = date.toDate();
                            String strDate = date1.toString();

                            HashMap riderpt1 = (HashMap) testing1.get("rider");
                            HashMap riderpt2 = (HashMap) riderpt1.get("user");
                            String riderEmail = (String) riderpt2.get("email");
                            //System.out.println("\nEmail of rider = " +riderEmail);

                            HashMap driverpt1 = (HashMap) testing1.get("driver");
                            HashMap driverpt2 = (HashMap) driverpt1.get("user");
                            String driverEmail = (String) driverpt2.get("email");
                            //System.out.println("\nEmail of driver = " +driverEmail);

                            String driverName = driverEmail;
                            String riderName = riderEmail;
                            if (email.equals(driverEmail)){
                                System.out.println("Logged user is Driver = "+driverEmail);
                                driverName="Yourself";
                                Rides obj = new Rides();
                                obj.setTimestamp(strDate);
                                obj.setDriver(driverName);
                                obj.setRider(riderName);
                                datalist.add(obj);
                            }
                            else if (email.equals(riderEmail)){
                                System.out.println("Logged user is Rider = "+riderEmail);
                                riderName="Yourself";
                                Rides obj = new Rides();
                                obj.setTimestamp(strDate);
                                obj.setDriver(driverName);
                                obj.setRider(riderName);
                                datalist.add(obj);
                            }

                            //Rides obj = d.toObject(Rides.class);

                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}