package com.HopIn;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Ride {

    private UserLocation Driver;
    private UserLocation Rider;
    private @ServerTimestamp Date timestamp;
    private String status;
    public Ride(UserLocation driver, UserLocation rider, Date timestamp) {
        Driver = driver;
        Rider = rider;
        this.timestamp = timestamp;
        this.status = "REQUESTED";
    }

    public Ride() {

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
