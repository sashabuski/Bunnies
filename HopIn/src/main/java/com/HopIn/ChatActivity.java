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

/**
 * This is the chat activity. It is accessible by the driver and rider during the pickup process.
 * The database stores a Messages array with an ID corresponding with the Ride ID.
 * This activity loads existing messages when opened, and loads updates i.e. new messages in real time.
 *
 */

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button send;
    private EditText messageBox;
    private String RideID;
    private MessageList messagesList = new MessageList();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userType;

    /**
     * OnCreate loads existing ride messages from database.
     * Sets up send onClickListener to send messages to the DATABASE
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userType = (String) getIntent().getSerializableExtra("userType");
        recyclerView = findViewById(R.id.recycler_view);
        RideID = (String) getIntent().getSerializableExtra("ReqID");
        messageBox = findViewById(R.id.chat_message);

        listenForMessages();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DocumentReference docRef = db.collection("Messages").document(RideID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            private static final String TAG = "TAG";
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        messagesList = document.toObject(MessageList.class);
                        filterIncomingMessages(messagesList);

                        CustomChatAdapter adapter = new CustomChatAdapter(ChatActivity.this, messagesList.getMessagesList());
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
                    messagesList.getMessagesList().add(new MessageModel(messageBox.getText().toString(), CustomChatAdapter.MESSAGE_TYPE_OUT, "Rider"));
                } else {
                    messagesList.getMessagesList().add(new MessageModel(messageBox.getText().toString(), CustomChatAdapter.MESSAGE_TYPE_OUT, "Driver"));
                }

                RideID = (String) getIntent().getSerializableExtra("ReqID");
                MessageList mL = new MessageList(messagesList.getMessagesList());
                CustomChatAdapter adapter = new CustomChatAdapter(ChatActivity.this, messagesList.getMessagesList());
                messageBox.setText("");
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(messagesList.getMessagesList().size() - 1);

                db.collection("Messages").document(RideID).set(mL).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });
    }

    /**
     * filterIncomingMessages method uses current user type to organise message list into
     * incoming and outgoing messages to be displayed appropriately in the RecyclerView.
     *
     *
     * @param ml
     */

    public void filterIncomingMessages(MessageList ml) {

        ArrayList<MessageModel> messages = ml.getMessagesList();

        if (userType.equals("Driver")) {

            for (MessageModel message : messages) {

                if (message.userType.equals("Rider")) {

                    message.messageType = CustomChatAdapter.MESSAGE_TYPE_IN;

                }else if(message.userType.equals("Driver")) {

                    message.messageType = CustomChatAdapter.MESSAGE_TYPE_OUT;
                }
            }
        } else if (userType.equals("Rider")) {

            for (MessageModel message : messages) {

                if (message.userType.equals("Driver")) {

                    message.messageType = CustomChatAdapter.MESSAGE_TYPE_IN;

                }else if(message.userType.equals("Rider")) {

                    message.messageType = CustomChatAdapter.MESSAGE_TYPE_OUT;
                }
            }
        }

    }

    /**
     * listenForMessages method implements snapshotListener to listen for new messages added to this rides messages collection.
     * onEvent, the whole message list is drawn from DB, filtered using filterIncomingMessages and displayed in the recyclerView.
     *
     */

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
                    CustomChatAdapter adapter = new CustomChatAdapter(ChatActivity.this, messagesList.getMessagesList());
                    recyclerView.setAdapter(adapter);
                    recyclerView.scrollToPosition(messagesList.getMessagesList().size() - 1);
                }
            }
        });
    }
}