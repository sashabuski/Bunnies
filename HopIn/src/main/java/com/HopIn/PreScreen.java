package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.SQLOutput;

public class PreScreen extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    User currentUser;
    TextView a,b,c,d,e,f;
    Switch zwitch;
    Button nextButton;
    Intent driverIntent;
    Intent riderIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_screen);
        currentUser = new User();
        zwitch = (Switch)findViewById(R.id.switch1);
        nextButton = (Button)findViewById(R.id.button);
        a = (TextView)findViewById(R.id.a);
        b = (TextView)findViewById(R.id.b);
        c = (TextView)findViewById(R.id.c);
        d = (TextView)findViewById(R.id.d);
        e = (TextView)findViewById(R.id.e);
        f = (TextView)findViewById(R.id.f);
        driverIntent = new Intent(this, DriverMapsActivity.class);

        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if(zwitch.isChecked()){
                    driverIntent.putExtra("loggedUser", currentUser);
                    startActivity(driverIntent);
                }else{
                    
                }

            }});
        DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);
                a.setText(currentUser.getEmail());
                b.setText(currentUser.getPassword());
                c.setText(currentUser.getfName());
                d.setText(currentUser.getlName());
                e.setText(currentUser.getCarModel());
                f.setText(currentUser.getCarNumber());
            }



        });


    }

    @Override
    public void onBackPressed(){
        FirebaseAuth.getInstance().signOut();
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Toast.makeText(PreScreen.this, "Logged out.", Toast.LENGTH_LONG).show();
    }
}