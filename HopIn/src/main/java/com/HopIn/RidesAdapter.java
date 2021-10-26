package com.HopIn;


import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class RidesAdapter extends RecyclerView.Adapter<RidesAdapter.RidesViewHolder> {

    private List<Rides> ridesList;

    public RidesAdapter(List<Rides> ridesList) {
        this.ridesList = ridesList;
    }

    @NonNull
    @Override
    public RidesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_rides, parent, false);
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_rides,parent,false);
        return new RidesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RidesViewHolder holder, int position) {
        Rides ride = ridesList.get(position);
        holder.textTimestamp.setText(ride.timestamp);
        //holder.textTimestamp.setText("timestamp");

        holder.textDriver.setText("Driver: " + ride.driver);
        //holder.textDriver.setText("manual driver");

        holder.textRider.setText("Rider: " + ride.rider);
        //holder.textRider.setText("manual rider");

        //holder.textPickup.setText("Pick up: " + ride.pickupPoint);
        holder.textPickup.setText("Pick up: N/A");
    }

    @Override
    public int getItemCount() {
        return ridesList.size();
    }

    class RidesViewHolder extends RecyclerView.ViewHolder {
        TextView textTimestamp, textDriver, textRider, textPickup;
        public RidesViewHolder(@NonNull View itemView) {
            super(itemView);
            textTimestamp = itemView.findViewById(R.id.t1);
            textDriver = itemView.findViewById(R.id.t2);
            textRider = itemView.findViewById(R.id.t3);
            textPickup = itemView.findViewById(R.id.t4);
        }
    }
}