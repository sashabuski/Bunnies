package com.HopIn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This register user activity registers a new user with firebase authentication.
 * Will not accept duplicate emails, will only accept emails ending with "@autuni.ac.nz"
 * +other input requirements
 *
 */
public class Register2 extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, repasswordEditText;
    private Button createButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        createButton = (Button)findViewById(R.id.login);
        emailEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        repasswordEditText = (EditText) findViewById(R.id.repassword);

        createButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                registerUser();
            }
        });
    }

    /**
     * Method will only save to db if all input specifications have been passed.
     * used in onClickListener
     */
    public void registerUser() {


        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repassword = repasswordEditText.getText().toString().trim();

        Intent intent;
        intent = new Intent(this, enterName.class);

        if(!password.equals(repassword)){
            repasswordEditText.setError("Passwords do not match.");
            repasswordEditText.requestFocus();
            return;
        }
        if(!email.endsWith("@autuni.ac.nz")){
            emailEditText.setError("This is not an AUT email address.");
            emailEditText.requestFocus();
            return;
        }
        if(email.isEmpty()){
            emailEditText.setError("Email address required.");
            emailEditText.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordEditText.setError("Password address required.");
            passwordEditText.requestFocus();
            return;
        }
        if(repassword.isEmpty()){
            repasswordEditText.setError("Re-enter your password.");
            repasswordEditText.requestFocus();
            return;
        }
        if(password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters.");
            passwordEditText.requestFocus();
            return;
        }

        User user = new User(email, password);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NotNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NotNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        Toast.makeText(Register2.this, "Account created.", Toast.LENGTH_LONG).show();

                                    }else{
                                        Toast.makeText(Register2.this, "Account creation failed.", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });

                            db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(user);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        }
                    }
                });
    return;
    }

}
