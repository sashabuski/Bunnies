package com.HopIn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

/**
 *
 * This activity is used to sign in the user by referencing the FireBase authentication system.
 *
 */
public class SignInActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private Button signInButton;
    private EditText emailEditText, passwordEditText;
    SharedPreferences sharedPreferences;
    public static final String filename = "login";
    public static final String Username = "username";
    //public static final String Password = "password";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        signInButton = (Button)findViewById(R.id.SignInButton);
        emailEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);

        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                signIn();
            }
        });

        sharedPreferences = getSharedPreferences(filename, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username))
        {
            Intent intent = new Intent(SignInActivity.this, PreScreen.class);
            startActivity(intent);
        }


    }

    public void signIn() {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent;
                    intent = new Intent(SignInActivity.this, Profile.class);
                   // intent.putExtra("email", "logged in as: "+email);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }else{
                    Toast.makeText(SignInActivity.this, "Failed to login. Please check your credentials", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}