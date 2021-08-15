package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class enterCar extends AppCompatActivity {

    private EditText carModelInput, carNumberInput;
    private Button nextButton, skipButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Intent nextIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_car);
        nextIntent = new Intent(this, PreScreen.class);
        skipButton = (Button) findViewById(R.id.skipButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        carModelInput = (EditText) findViewById(R.id.carModel);
        carNumberInput = (EditText) findViewById(R.id.carNumber);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addCarToDB();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(nextIntent);
            }
        });

    }

    @Override
    public void onBackPressed(){

    }

    public void addCarToDB(){
            Intent i = getIntent();


            User user = (User)(i.getSerializableExtra("user"));

            String carModel = carModelInput.getText().toString().trim();
            String carNumber = carNumberInput.getText().toString().trim();

            if(carModel.isEmpty()){
                carModelInput.setError("Model required to submit");
                carModelInput.requestFocus();
                return;
            }
            if(carNumber.isEmpty()) {
                carNumberInput.setError("Number plate required to submit.");
                carNumberInput.requestFocus();
                return;
            }

            user.setCarModel(carModel);
            user.setCarNumber(carNumber);

            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .set(user, SetOptions.merge());

            nextIntent.putExtra("user", user);
            startActivity(nextIntent);
        }
    }
