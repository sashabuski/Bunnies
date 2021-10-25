package com.HopIn;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * UserLocation object is created when the user sends their location
 * to the database, these objects should only be stored in the DB when
 * the user is using the app, and should be removed when they are not, to
 * ensure the DB only contains current riders/drivers.
 *
 */
public class UserLocation implements Serializable {

    private User user;
    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private float bearing;

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getBearing() {
        return bearing;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {

        this.user = user;
    }

    public GeoPoint getGeoPoint() {

        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {

        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public UserLocation(){

    }

    public UserLocation(User user, GeoPoint geoPoint) {
        this.user = user;
        this.geoPoint = geoPoint;

    }

    public UserLocation(User user) {
        this.user = user;
        this.geoPoint = null;

    }

}
