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

import java.io.Serializable;

/**
 * This Activity requests the users name to be entered by the user when registering
 * their account, and saves it to the User db object.
 */
public class enterName extends AppCompatActivity implements Serializable {

    private EditText firstNameInput, lastNameInput;
    private Button nextButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User user;
    private Intent nextIntent;
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);

        nextButton = (Button)findViewById(R.id.nextButton);
        firstNameInput = (EditText) findViewById(R.id.firstName);
        lastNameInput = (EditText) findViewById(R.id.lastName);

        Intent i = getIntent();

        nextIntent = new Intent(this, enterCar.class);

        user = (User)(i.getSerializableExtra("user"));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addNameToDB();

            }
        });
    }

    @Override
    public void onBackPressed(){

    }

    public void addNameToDB(){



        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();

        if(firstName.isEmpty()){
            firstNameInput.setError("First name required.");
            firstNameInput.requestFocus();
            return;
        }
        if(lastName.isEmpty()) {
            lastNameInput.setError("Last name required.");
            lastNameInput.requestFocus();
            return;
        }


        db.collection("Users").document(mAuth.getCurrentUser().getUid())
                .update(
                        "fName", firstName,
                        "lName", lastName
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                System.out.println("you did it");
            }
        });


        nextIntent.putExtra("user", user);
        startActivity(nextIntent);
    }
}