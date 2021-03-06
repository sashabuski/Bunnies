package com.HopIn;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.SQLOutput;

/**
 * This activity is displayed before the map is open and gives the
 * user the option to open either the driver map or the rider map via the
 * switch that changes the destination activity of the next button.
 *
 */
public class PreScreen extends AppCompatActivity {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();

    User currentUser;
    TextView a,b,c,d,e,f;
    Switch zwitch;
    Button nextButton;
    Intent driverIntent;
    Intent riderIntent;
    Button profileButton;
    Intent profileIntent;
    RoundImageView roundView;
    Button listButton;

    private Object SaveSharedPreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_screen);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        currentUser = new User();
        zwitch = (Switch)findViewById(R.id.switch1);

        a = (TextView)findViewById(R.id.a);
        b = (TextView)findViewById(R.id.b);
        c = (TextView)findViewById(R.id.c);
        d = (TextView)findViewById(R.id.d);
        e = (TextView)findViewById(R.id.e);
        f = (TextView)findViewById(R.id.f);

        driverIntent = new Intent(this, DriverMapsActivity.class);
        riderIntent = new Intent(this, RiderMapsActivity.class);

        nextButton = (Button)findViewById(R.id.button);
        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
				
				boolean checkNum = (currentUser.getCarNumber().toString().equals("")) || (currentUser.getCarNumber().toString().equals("N/A"));
                boolean checkMod = (currentUser.getCarModel().toString().equals("")) || (currentUser.getCarModel().toString().equals("N/A"));
                
				if(zwitch.isChecked()){
					if (checkNum && checkMod) {
                        Toast.makeText(getApplicationContext(), "ERROR 404! Car number plate & Model not found." + "\nUpdate car details to continue.", Toast.LENGTH_LONG).show();
                    }
                    else if(checkNum){
                        Toast.makeText(getApplicationContext(), "ERROR 404! Car number plate not found." + "\nUpdate car number plate to continue.", Toast.LENGTH_LONG).show();
					}
					else if (checkMod){
                        Toast.makeText(getApplicationContext(), "ERROR 404! Car model not found." + "\nUpdate car model to continue.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        driverIntent.putExtra("loggedUser", currentUser);
                        startActivity(driverIntent);
						overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }                    
                }else{
                    riderIntent.putExtra("loggedUser", currentUser);
                    startActivity(riderIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }

            }});


        roundView = findViewById(R.id.imgView);

        DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);
                a.setText(currentUser.getEmail());
                //b.setText(currentUser.getPassword());
                b.setText("Password Not Displayed");
                c.setText(currentUser.getfName());
                d.setText(currentUser.getlName());
                e.setText(currentUser.getCarModel());
                f.setText(currentUser.getCarNumber());

            }
        });

        profileButton = findViewById(R.id.btnProfile);
        profileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openProfile();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        listButton = findViewById(R.id.btnList);
        listButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openList();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to LOGOUT?");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        FirebaseAuth.getInstance().signOut();
   

                        Intent intent = new Intent(PreScreen.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                        Toast.makeText(PreScreen.this, "Logged out.", Toast.LENGTH_LONG).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
                Toast.makeText(PreScreen.this, "You clicked the no button", Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void openProfile(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void openList(){
        Intent intent = new Intent(this, ListRides.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        startActivity(getIntent());
    }
}