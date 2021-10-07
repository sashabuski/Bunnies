package com.HopIn;
/**
 *
 * This activity is opened when starting the app, It gives the user options to
 * create an account or sign in, with buttons that take the user to either
 * activity.
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    private Button SignInButton;
    private Button createAccountButton;
    FirebaseAuth mAuth;
    FirebaseUser muser;
    FirebaseFirestore db;
    public static final String filename = "login";
    public static final String Username = "username";
    TextView textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        /*sharedPreferences = getSharedPreferences(filename, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username)){
            textMessage.setText("Hello " + sharedPreferences.getString(Username, ""));
        }*/


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createAccountButton = findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                openCreateAccount();

            }
        });

        SignInButton = findViewById(R.id.SignInButton);
        SignInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                openSignIn();
            }
        });


    }

    public void openSignIn(){

        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void openCreateAccount(){

        Intent intent = new Intent(this, Register2.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}