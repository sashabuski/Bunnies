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

public class enterName extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput;
    private Button nextButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);

        nextButton = (Button)findViewById(R.id.nextButton);
        firstNameInput = (EditText) findViewById(R.id.firstName);
        lastNameInput = (EditText) findViewById(R.id.lastName);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addNameToDB();

            }
        });
    }

    public void addNameToDB(){
        Intent i = getIntent();
        Intent nextIntent = new Intent(this, enterCar.class);

        User user = (User)(i.getSerializableExtra("user"));

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

        user.setfName(firstName);
        user.setlName(lastName);

        db.collection("Users").document(mAuth.getCurrentUser().getUid())
                .set(user, SetOptions.merge());

        nextIntent.putExtra("user", user);
        startActivity(nextIntent);
    }
}