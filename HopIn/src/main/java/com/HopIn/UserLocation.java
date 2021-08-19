package com.HopIn;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * UserLocation object is created when the user sends their location
 * to the database, these objects should only be stored in the DB when
 * the user is using the app, and should be removed when they are not, to
 * ensure the DB only contains current riders/drivers.
 *
 */
public class UserLocation {

    private User user;
    private GeoPoint geoPoint;
   // private @ServerTimestamp String timestamp;

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

  //  public String getTimestamp() {
  //      return timestamp;
   // }

  //  public void setTimestamp(String timestamp) {
       // this.timestamp = timestamp;
   // }

    public UserLocation(){

    }

    public UserLocation(User user, GeoPoint geoPoint) {
        this.user = user;
        this.geoPoint = geoPoint;
      //  this.timestamp = timestamp;
    }
    public UserLocation(User user) {
        this.user = user;
        this.geoPoint = null;
        //this.timestamp = null;
    }

}
