package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class enterCar extends AppCompatActivity {

    private EditText carModelInput, carNumberInput;
    private Button nextButton, skipButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_car);

        nextButton = (Button)findViewById(R.id.nextButton);
        carModelInput = (EditText) findViewById(R.id.firstName);
        carNumberInput = (EditText) findViewById(R.id.lastName);

    }
}