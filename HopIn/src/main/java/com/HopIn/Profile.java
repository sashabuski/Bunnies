package com.HopIn;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    FirebaseFirestore db;

    FirebaseAuth mAuth;
    User currentUser;

    private Button updateButton;
    private TextView txtWelcome;
    TextView txtEmail;

    private TextView txtFirstName;
    private TextView txtLastName;
    private TextView txtPhone;
    private TextView txtNumberPlate;
    private TextView txtCarModel;
    private TextView txtConfirmation;

    ImageView profileImage;
    StorageReference storageReference;
    private static final int GALLERY_INTENT_CODE = 1023;


    AlertDialog dialogEmail;
    AlertDialog dialogFirstName;
    AlertDialog dialogLastName;
    AlertDialog dialogNumberPlate;
    AlertDialog dialogCarModel;
    AlertDialog dialogConfirmation;

    EditText editEmail;
    EditText editFirstName;
    EditText editLastName;
    EditText editNumberPlate;
    EditText editCarModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        profileImage = findViewById(R.id.profileImage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileReference = storageReference.child(("users/" + mAuth.getCurrentUser().getUid()+"/profile.jpg"));
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery, 1000);

            }
        });


        currentUser = new User();
        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtFirstName = (TextView) findViewById(R.id.txtFirstName);
        txtLastName = (TextView) findViewById(R.id.txtLastName);
        //txtPhone = (TextView)findViewById(R.id.txtPhone);
        txtNumberPlate = (TextView) findViewById(R.id.txtNumberPlate);
        txtCarModel = (TextView) findViewById(R.id.txtCarModel);
        //txtConfirmation = (TextView)findViewById(R.id.txtConfirmation);
        //txtConfirmation.setText("Are you sure?");
        updateButton = (Button) findViewById(R.id.btnUpdate);


        dialogEmail = new AlertDialog.Builder(this).create();
        dialogFirstName = new AlertDialog.Builder(this).create();
        dialogLastName = new AlertDialog.Builder(this).create();
        dialogNumberPlate = new AlertDialog.Builder(this).create();
        dialogCarModel = new AlertDialog.Builder(this).create();

        dialogConfirmation = new AlertDialog.Builder(this).create();
        


        editEmail = new EditText(this);
        editFirstName = new EditText(this);
        editLastName = new EditText(this);
        editNumberPlate = new EditText(this);
        editCarModel = new EditText(this);

        dialogEmail.setTitle("Update Email");
        dialogEmail.setView(editEmail);
        dialogFirstName.setTitle("Update First Name");
        dialogFirstName.setView(editFirstName);
        dialogLastName.setTitle("Update Last Name");
        dialogLastName.setView(editLastName);
        dialogNumberPlate.setTitle("Update Number Plate");
        dialogNumberPlate.setView(editNumberPlate);
        dialogCarModel.setTitle("Update Car Model");
        dialogCarModel.setView(editCarModel);
        dialogConfirmation.setTitle("Confirmation");
        dialogConfirmation.setMessage("Are you sure you want to update your details?");


        DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);

                txtWelcome.setText(currentUser.getfName() + "'s Profile");

                txtEmail.setText(currentUser.getEmail());
                txtFirstName.setText(currentUser.getfName());
                txtLastName.setText(currentUser.getlName());
                //txtPhone.setText(currentUser.getPhone());
                txtNumberPlate.setText(currentUser.getCarNumber());

                if (txtNumberPlate.getText().toString().equals("")) {
                    txtNumberPlate.setText(("N/A"));
                }
                txtCarModel.setText(currentUser.getCarModel());
                if (txtCarModel.getText().toString().equals("")) {
                    txtCarModel.setText(("N/A"));
                }

            }
        });

        dialogFirstName.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                txtFirstName.setText(editFirstName.getText());
                txtFirstName.setError(null);
            }
        });
        txtFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editFirstName.setText(txtFirstName.getText());
                dialogFirstName.show();
            }
        });

        dialogLastName.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                txtLastName.setText(editLastName.getText());
                txtLastName.setError(null);
            }
        });
        txtLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editLastName.setText(txtLastName.getText());
                dialogLastName.show();
            }
        });

        dialogNumberPlate.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                txtNumberPlate.setText(editNumberPlate.getText());

            }
        });
        txtNumberPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNumberPlate.setText(txtNumberPlate.getText());
                dialogNumberPlate.show();
            }
        });


        dialogCarModel.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                txtCarModel.setText(editCarModel.getText());
            }
        });
        txtCarModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCarModel.setText(txtCarModel.getText());
                dialogCarModel.show();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirmation.show();
            }
        });
        dialogConfirmation.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateUser();
            }
        });


    }


    @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1000) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    profileImage.setImageURI(imageUri);

                    uploadImageToFirebase(imageUri);
                }
            }
        }



        public void updateUser () {
            if (!isNameValid(txtFirstName.getText().toString())) {
                txtFirstName.setError("First Name is required.");
                txtFirstName.requestFocus();
                return;
            }
            //txtEmail.setError(null);
            if (txtLastName.getText().toString().equals("")) {
                txtLastName.setError("Last Name is required.");
                txtLastName.requestFocus();
                return;
            }

            String firstName = txtFirstName.getText().toString().trim();
            String lastName = txtLastName.getText().toString().trim();
            String carModel = txtCarModel.getText().toString().trim();
            String carNumberPlate = txtNumberPlate.getText().toString().trim();

            currentUser.setfName(firstName);
            currentUser.setlName(lastName);
            currentUser.setCarModel(carModel);
            currentUser.setCarNumber(carNumberPlate);

            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .set(currentUser, SetOptions.merge());

            finish();
            startActivity(getIntent());
        }
        private void uploadImageToFirebase (Uri imageUri) {
            //upload user's profile image to firebase storage
            final StorageReference fileRef = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(profileImage);
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Profile.this, "Image Failed", Toast.LENGTH_SHORT).show();
                }
            });

        }

        public boolean isNameValid(String s) {
            if (s.equals("")) {
                return false;
            }
            else if (s.trim().equals("")) {
                return false;
            }
            return true;
        }

    }

