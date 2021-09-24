package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * This Activity requests the car details to be entered by the user when registering
 * their account, and saves it to the User db object. This is optional information and
 * the user can use the skip button as car details wont be necessary for passengers only.
 *
 */
public class enterCar extends AppCompatActivity {

    private EditText carModelInput, carNumberInput;
    private Button nextButton, skipButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    /**
     * Method prevents back button default function
     * (disable back button)
     */
    @Override
    public void onBackPressed(){

    }

    /**
     *Method Used in button onClickListener
     *
     */
    public void addCarToDB(){


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



        db.collection("Users").document(mAuth.getCurrentUser().getUid())
                .update(
                        "carModel", carModel,
                        "carNumber", carNumber
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                System.out.println("you did it");
            }
        });


            startActivity(nextIntent);
        }
    }
