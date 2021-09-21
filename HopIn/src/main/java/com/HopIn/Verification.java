package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.emulators.EmulatedServiceSettings;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import android.os.Bundle;

public class Verification extends AppCompatActivity
{

    EditText emailEditText, passwordEditText, repasswordEditText;
    FirebaseFirestore db;
    String currentuserID;
    Button resendCode;
    TextView verifyMsg;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        FirebaseAuth mAuth;

        Intent intent;
        intent = new Intent(this, enterName.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        resendCode = findViewById(R.id.resendCode);
        verifyMsg = findViewById(R.id.verifyMsg);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(v.getContext(),
                                        "Verification Email has been sent.",
                                        Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);

                                currentuserID = mAuth.getCurrentUser().getUid();

                                return;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                                return;
                            }
                        });

                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NotNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(Verification.this, "Account created.", Toast.LENGTH_LONG).show();
                                FirebaseUser user = mAuth.getCurrentUser();

                            }else{
                                Toast.makeText(Verification.this, "Account creation failed.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                    db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(user);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }

            });



    }
}
