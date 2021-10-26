package com.HopIn;

import com.google.firebase.Timestamp;
import com.google.firebase.database.GenericTypeIndicator;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;

public class Rides {
    public String status;
    public String timestamp;
    public String driver, pickup, rider;

    public Rides() {
    }

    public Rides(String status, String timestamp, String driver, String pickup, String rider) {
        this.status = status;
        this.timestamp = timestamp;
        this.driver = driver;
        this.pickup = pickup;
        this.rider = rider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getRider() {
        return rider;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }
}
