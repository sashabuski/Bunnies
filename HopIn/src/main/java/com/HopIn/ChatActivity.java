package com.HopIn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button send;
    EditText fuckyou;
    String RideID;
    ArrayList<MessageModel> cdikendiednhuiehf = new ArrayList<>();
    MessageList messagesList = new MessageList();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userType = (String) getIntent().getSerializableExtra("userType");

        recyclerView = findViewById(R.id.recycler_view);
        // CustomAdapter adapter = new CustomAdapter(this, messagesList.getMessagesList());
        RideID = (String) getIntent().getSerializableExtra("ReqID");

        listenForMessages();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(adapter);

        DocumentReference docRef = db.collection("Messages").document(RideID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            private static final String TAG = " just go fuck yourself";

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        messagesList = document.toObject(MessageList.class);

                        filterIncomingMessages(messagesList);

                        CustomAdapter adapter = new CustomAdapter(ChatActivity.this, messagesList.getMessagesList());


                        // adapter.setMessageList(messagesList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.scrollToPosition(messagesList.getMessagesList().size() - 1);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        send = findViewById(R.id.chat_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (userType.equals("Rider")) {
                    messagesList.getMessagesList().add(new MessageModel(fuckyou.getText().toString(), CustomAdapter.MESSAGE_TYPE_OUT, "Rider"));
                } else {
                    messagesList.getMessagesList().add(new MessageModel(fuckyou.getText().toString(), CustomAdapter.MESSAGE_TYPE_OUT, "Driver"));
                }


                RideID = (String) getIntent().getSerializableExtra("ReqID");

                MessageList mL = new MessageList(messagesList.getMessagesList());

                CustomAdapter adapter = new CustomAdapter(ChatActivity.this, messagesList.getMessagesList());
                fuckyou.setText("");
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(messagesList.getMessagesList().size() - 1);

                db.collection("Messages").document(RideID).set(mL).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });

            }
        });

        fuckyou = findViewById(R.id.chat_message);
    }


    public void filterIncomingMessages(MessageList ml) {


        ArrayList<MessageModel> messages = ml.getMessagesList();

        if (userType.equals("Driver")) {

            for (MessageModel message : messages) {

                if (message.userType.equals("Rider")) {

                    message.messageType = CustomAdapter.MESSAGE_TYPE_IN;
                }else if(message.userType.equals("Driver")) {

                    message.messageType = CustomAdapter.MESSAGE_TYPE_OUT;
                }
            }
        } else if (userType.equals("Rider")) {

            for (MessageModel message : messages) {

                if (message.userType.equals("Driver")) {

                    message.messageType = CustomAdapter.MESSAGE_TYPE_IN;
                }else if(message.userType.equals("Rider")) {

                    message.messageType = CustomAdapter.MESSAGE_TYPE_OUT;
                }
            }
        }

    }


    public void listenForMessages() {
        FirebaseFirestore.getInstance()
                .collection("Messages").document(RideID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error != null) {

                    return;
                }
                if (value.exists()) {

                    messagesList = value.toObject(MessageList.class);

                    filterIncomingMessages(messagesList);

                    CustomAdapter adapter = new CustomAdapter(ChatActivity.this, messagesList.getMessagesList());


                    // adapter.setMessageList(messagesList);
                    recyclerView.setAdapter(adapter);
                    recyclerView.scrollToPosition(messagesList.getMessagesList().size() - 1);

                }
            }
        });
    }
}