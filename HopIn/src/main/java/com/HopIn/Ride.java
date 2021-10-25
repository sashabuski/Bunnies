package com.HopIn;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Ride Object
 *
 */
public class Ride implements Serializable {

    private UserLocation Driver;
    private UserLocation Rider;
    private @ServerTimestamp Date timestamp;
    private String status;
    private PickupPt pickupPoint;

    public Ride(UserLocation driver, UserLocation rider, Date timestamp, PickupPt pickupPoint) {
        this.Driver = driver;
        this.Rider = rider;
        this.pickupPoint = pickupPoint;
        this.timestamp = timestamp;
        this.status = "REQUESTED";
    }

    public Ride() {
        this.Driver = null;
        this.Rider = null;
        this.pickupPoint = null;
        this.timestamp = null;
        this.status = null;
    }

    public PickupPt getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(PickupPt pickupPoint) {
        this.pickupPoint = pickupPoint;
    }
    public UserLocation getDriver() {
        return Driver;
    }

    public void setDriver(UserLocation driver) {
        Driver = driver;
    }

    public UserLocation getRider() {
        return Rider;
    }

    public void setRider(UserLocation rider) {
        Rider = rider;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
