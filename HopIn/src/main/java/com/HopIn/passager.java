package com.HopIn;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class passager extends AppCompatActivity {

    private TextView pname;
    private TextView pupp;
    private TextView destination;
    private TextView pt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passager);

        pname = findViewById(R.id.pname);
        pupp = findViewById(R.id.pupp);
        destination = findViewById(R.id.destination);
        pt = findViewById(R.id.pt);

        String str_pname=getIntent().getStringExtra("pname");
        pname.setText("Passager Name : "+str_pname);
        String str_pupp = getIntent().getStringExtra("pupp");
        pupp.setText("Pick Up point : "+str_pupp);
        String str_dest=getIntent().getStringExtra("destination");
        destination.setText("Destination : "+str_dest);
        String str_pt = getIntent().getStringExtra("pt");
        pt.setText("Parking/Tip : "+str_pt);
    }
}