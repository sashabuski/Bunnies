package com.HopIn;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * RidesAdapter is used for the RecyclerView implemented to display past rides for logged in user
 *
 */

public class RidesAdapter extends RecyclerView.Adapter<RidesAdapter.RidesViewHolder> {

    private List<Rides> ridesList;
    public RidesAdapter(List<Rides> ridesList) {
        this.ridesList = ridesList;
    }

    @NonNull
    @Override
    public RidesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_rides,parent,false);
        return new RidesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RidesViewHolder holder, int position) {
        Rides ride = ridesList.get(position);
        holder.textTimestamp.setText(ride.timestamp);
        holder.textDName.setText("Driver Name: "+ride.driverN);
        holder.textDEmail.setText("Driver Email: " + ride.driverE);
        holder.textRName.setText("Rider Name: "+ride.riderN);
        holder.textREmail.setText("Rider email: " + ride.riderE);
        holder.textPickup.setText("Pick up location: "+ ride.pickup);
    }

    @Override
    public int getItemCount() {
        return ridesList.size();
    }

    class RidesViewHolder extends RecyclerView.ViewHolder {
        TextView textTimestamp, textDName,textDEmail, textRName, textREmail, textPickup;
        public RidesViewHolder(@NonNull View itemView) {
            super(itemView);
            textTimestamp = itemView.findViewById(R.id.t1);
            textDName = itemView.findViewById(R.id.t5);
            textDEmail = itemView.findViewById(R.id.t2);
            textRName = itemView.findViewById(R.id.t6);
            textREmail = itemView.findViewById(R.id.t3);
            textPickup = itemView.findViewById(R.id.t4);
        }
    }
}